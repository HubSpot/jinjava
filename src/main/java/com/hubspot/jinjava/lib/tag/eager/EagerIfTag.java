package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValue;
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

  @Override
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
    final int deferredPosition = interpreter.getPosition();
    // If the branch is impossible, it should be removed.
    boolean definitelyDrop = shouldDropBranch(tagNode, interpreter, deferredLineNumber);
    // If an ("elseif") branch would definitely get executed,
    // change it to an "else" tag and drop all the subsequent branches.
    // We know this has to start as false otherwise IfTag would have chosen
    // the first branch.
    boolean definitelyExecuted = false;
    StringBuilder sb = new StringBuilder();
    sb.append(
      getEagerImage(
        buildToken(tagNode, e, deferredLineNumber, deferredPosition),
        interpreter
      )
    );
    int branchStart = 0;
    int childrenSize = tagNode.getChildren().size();
    while (branchStart < childrenSize) {
      int branchEnd = findNextElseToken(tagNode, branchStart);
      if (!definitelyDrop) {
        int finalBranchStart = branchStart;
        EagerExecutionResult result = executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              evaluateBranch(tagNode, finalBranchStart, branchEnd, interpreter)
            ),
          interpreter,
          false,
          false,
          true
        );
        sb.append(result.getResult());
        resetBindingsForNextBranch(interpreter, result);
      }
      if (branchEnd >= childrenSize || definitelyExecuted) {
        break;
      }
      TagNode caseNode = (TagNode) tagNode.getChildren().get(branchEnd);
      definitelyDrop =
        caseNode.getName().equals(ElseIfTag.TAG_NAME) &&
        shouldDropBranch(caseNode, interpreter, deferredLineNumber);
      if (!definitelyDrop) {
        definitelyExecuted =
          caseNode.getName().equals(ElseTag.TAG_NAME) ||
          isDefinitelyExecuted(caseNode, interpreter, deferredLineNumber);
        if (definitelyExecuted) {
          sb.append(
            String.format(
              "%s else %s",
              caseNode.getSymbols().getExpressionStartWithTag(),
              caseNode.getSymbols().getExpressionEndWithTag()
            )
          );
        } else {
          sb.append(
            getEagerImage(
              buildToken(caseNode, e, deferredLineNumber, deferredPosition),
              interpreter
            )
          );
        }
      }
      branchStart = branchEnd + 1;
    }
    return sb.toString();
  }

  private void resetBindingsForNextBranch(
    JinjavaInterpreter interpreter,
    EagerExecutionResult result
  ) {
    result
      .getSpeculativeBindings()
      .keySet()
      .stream()
      .filter(
        key ->
          interpreter.getContext().containsKey(key) &&
          interpreter.getContext().get(key) instanceof DeferredValue
      )
      .forEach(
        key -> {
          if (
            ((DeferredValue) interpreter.getContext().get(key)).getOriginalValue() != null
          ) {
            interpreter
              .getContext()
              .put(
                key,
                ((DeferredValue) interpreter.getContext().get(key)).getOriginalValue()
              );
          }
        }
      );
  }

  private String evaluateBranch(
    TagNode tagNode,
    int startIdx,
    int endIdx,
    JinjavaInterpreter interpreter
  ) {
    StringBuilder sb = new StringBuilder();
    for (int i = startIdx; i < endIdx; i++) {
      Node child = tagNode.getChildren().get(i);
      sb.append(child.render(interpreter).getValue());
    }
    return sb.toString();
  }

  private int findNextElseToken(TagNode tagNode, int startIdx) {
    int i;
    for (i = startIdx; i < tagNode.getChildren().size(); i++) {
      Node childNode = tagNode.getChildren().get(i);
      if (
        (TagNode.class.isAssignableFrom(childNode.getClass())) &&
        childNode.getName().equals(ElseIfTag.TAG_NAME) ||
        childNode.getName().equals(ElseTag.TAG_NAME)
      ) {
        return i;
      }
    }
    return i;
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
