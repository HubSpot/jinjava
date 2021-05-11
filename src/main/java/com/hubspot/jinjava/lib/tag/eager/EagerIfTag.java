package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ElseIfTag;
import com.hubspot.jinjava.lib.tag.ElseTag;
import com.hubspot.jinjava.lib.tag.IfTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.ObjectTruthValue;
import org.apache.commons.lang3.StringUtils;

public class EagerIfTag extends EagerTagDecorator<IfTag> {

  public EagerIfTag() {
    super(new IfTag());
  }

  public EagerIfTag(IfTag ifTag) {
    super(ifTag);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return getTag().interpret(tagNode, interpreter);
    } catch (DeferredValueException | TemplateSyntaxException e) {
      try {
        return wrapInAutoEscapeIfNeeded(
          eagerInterpret(tagNode, interpreter, e),
          interpreter
        );
      } catch (OutputTooBigException e1) {
        interpreter.addError(TemplateError.fromOutputTooBigException(e1));
        throw new DeferredValueException(
          String.format("Output too big for eager execution: %s", e1.getMessage())
        );
      }
    }
  }

  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (StringUtils.isBlank(tagNode.getHelpers())) {
      throw new TemplateSyntaxException(
        interpreter,
        tagNode.getMaster().getImage(),
        "Tag 'if' expects expression"
      );
    }

    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    result.append(
      executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              eagerRenderBranches(tagNode, eagerInterpreter, e)
            ),
          interpreter,
          false,
          false,
          true
        )
        .asTemplateString()
    );
    tagNode.getMaster().setRightTrimAfterEnd(false);
    result.append(reconstructEnd(tagNode));

    return result.toString();
  }

  public String eagerRenderBranches(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    // line number of the last attempted resolveELExpression
    final int deferredLineNumber = interpreter.getLineNumber();
    // If the branch is impossible, it should be removed.
    boolean definitelyDrop = shouldDropBranch(tagNode, interpreter, deferredLineNumber);
    // If an ("elseif") branch would definitely get executed,
    // change it to an "else" tag and drop all the subsequent branches.
    // We know this has to start as false otherwise IfTag would have chosen
    // the first branch.
    boolean definitelyExecuted = false;
    StringBuilder sb = new StringBuilder();
    sb.append(buildImage(tagNode, interpreter, e, deferredLineNumber));

    for (Node child : tagNode.getChildren()) {
      if (TagNode.class.isAssignableFrom(child.getClass())) {
        TagNode childTagNode = (TagNode) child;
        if (
          childTagNode.getName().equals(ElseIfTag.TAG_NAME) ||
          childTagNode.getName().equals(ElseTag.TAG_NAME)
        ) {
          if (definitelyExecuted) {
            break;
          }
          definitelyDrop =
            childTagNode.getName().equals(ElseIfTag.TAG_NAME) &&
            shouldDropBranch(childTagNode, interpreter, deferredLineNumber);
          if (!definitelyDrop) {
            definitelyExecuted =
              childTagNode.getName().equals(ElseTag.TAG_NAME) ||
              isDefinitelyExecuted(childTagNode, interpreter, deferredLineNumber);
            if (definitelyExecuted) {
              sb.append(
                String.format(
                  "%s else %s",
                  childTagNode.getSymbols().getExpressionStartWithTag(),
                  childTagNode.getSymbols().getExpressionEndWithTag()
                )
              );
            } else {
              sb.append(buildImage(childTagNode, interpreter, e, deferredLineNumber));
            }
          }
          continue;
        }
      }
      if (!definitelyDrop) {
        sb.append(child.render(interpreter).getValue());
      }
    }
    return sb.toString();
  }

  private String buildImage(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e,
    int deferredLineNumber
  ) {
    if (
      e instanceof DeferredParsingException &&
      deferredLineNumber == tagNode.getLineNumber()
    ) {
      return String.format(
        "%s %s %s %s", // {% elif deferred %}
        tagNode.getSymbols().getExpressionStartWithTag(),
        tagNode.getName(),
        ((DeferredParsingException) e).getDeferredEvalResult(),
        tagNode.getSymbols().getExpressionEndWithTag()
      );
    }
    return getEagerImage(tagNode.getMaster(), interpreter);
  }

  private boolean shouldDropBranch(
    TagNode tagNode,
    JinjavaInterpreter eagerInterpreter,
    int deferredLineNumber
  ) {
    if (deferredLineNumber > tagNode.getLineNumber()) {
      return true; // Deferred value thrown on a later branch so we can drop this one.
    } else if (deferredLineNumber == tagNode.getLineNumber()) {
      return false;
    }
    try {
      return !ObjectTruthValue.evaluate(
        eagerInterpreter.resolveELExpression(
          tagNode.getHelpers(),
          tagNode.getLineNumber()
        )
      );
    } catch (DeferredValueException e) {
      return false;
    }
  }

  private boolean isDefinitelyExecuted(
    TagNode tagNode,
    JinjavaInterpreter eagerInterpreter,
    int deferredLineNumber
  ) {
    if (deferredLineNumber == tagNode.getLineNumber()) {
      return false; // Deferred value thrown when checking if this branch would be executed.
    }
    try {
      return ObjectTruthValue.evaluate(
        eagerInterpreter.resolveELExpression(
          tagNode.getHelpers(),
          tagNode.getLineNumber()
        )
      );
    } catch (DeferredValueException e) {
      return false;
    }
  }
}
