package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Beta
public abstract class EagerTagDecorator<T extends Tag> implements Tag {
  private final T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  public T getTag() {
    return tag;
  }

  @Override
  public final String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      String output = innerInterpret(tagNode, interpreter);
      JinjavaInterpreter.checkOutputSize(output);
      return output;
    } catch (DeferredValueException | TemplateSyntaxException | OutputTooBigException e) {
      try {
        return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
          eagerInterpret(
            tagNode,
            interpreter,
            e instanceof InterpretException
              ? (InterpretException) e
              : new InterpretException("Exception with default render", e)
          ),
          interpreter
        );
      } catch (OutputTooBigException e1) {
        throw new DeferredValueException(
          String.format("Output too big for eager execution: %s", e1.getMessage())
        );
      }
    }
  }

  protected String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return tag.interpret(tagNode, interpreter);
  }

  @Override
  public String getName() {
    return tag.getName();
  }

  @Override
  public String getEndTagName() {
    return tag.getEndTagName();
  }

  @Override
  public boolean isRenderedInValidationMode() {
    return tag.isRenderedInValidationMode();
  }

  /**
   * Return the string value of interpreting this tag node knowing that
   * a deferred value has been encountered.
   * The tag node can not simply get evaluated normally in this circumstance.
   * @param tagNode TagNode to interpret.
   * @param interpreter The JinjavaInterpreter.
   * @param e The exception that required non-default interpretation. May be null
   * @return The string result of performing an eager interpretation of the TagNode
   */
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );
    result.append(
      EagerContextWatcher
        .executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              getEagerImage(
                buildToken(
                  tagNode,
                  e,
                  interpreter.getLineNumber(),
                  interpreter.getPosition()
                ),
                eagerInterpreter
              ) +
              renderChildren(tagNode, eagerInterpreter)
            ),
          interpreter,
          EagerContextWatcher
            .EagerChildContextConfig.newBuilder()
            .withForceDeferredExecutionMode(true)
            .build()
        )
        .asTemplateString()
    );

    if (StringUtils.isNotBlank(tagNode.getEndName())) {
      result.append(EagerReconstructionUtils.reconstructEnd(tagNode));
    }

    return result.toString();
  }

  public TagToken buildToken(
    TagNode tagNode,
    InterpretException e,
    int deferredLineNumber,
    int deferredPosition
  ) {
    if (
      e instanceof DeferredParsingException &&
      deferredLineNumber == tagNode.getLineNumber() &&
      deferredPosition == tagNode.getStartPosition()
    ) {
      return new TagToken(
        String.format(
          "%s %s %s %s", // like {% elif deferred %}
          tagNode.getSymbols().getExpressionStartWithTag(),
          tagNode.getName(),
          ((DeferredParsingException) e).getDeferredEvalResult(),
          tagNode.getSymbols().getExpressionEndWithTag()
        ),
        tagNode.getLineNumber(),
        tagNode.getStartPosition(),
        tagNode.getSymbols()
      );
    }
    return (TagToken) tagNode.getMaster();
  }

  /**
   * Render all children of this TagNode.
   * @param tagNode TagNode to render the children of.
   * @param interpreter The JinjavaInterpreter.
   * @return the string output of this tag node's children.
   */
  public String renderChildren(TagNode tagNode, JinjavaInterpreter interpreter) {
    StringBuilder sb = new StringBuilder();
    for (Node child : tagNode.getChildren()) {
      sb.append(child.render(interpreter).getValue());
    }
    return sb.toString();
  }

  /**
   * Casts token to TagToken if possible to get the eager image of the token.
   * @see #getEagerTagImage(TagToken, JinjavaInterpreter)
   * @param token Token to cast.
   * @param interpreter The Jinjava interpreter.
   * @return The image of the token which has been evaluated as much as possible.
   */
  public final String getEagerImage(Token token, JinjavaInterpreter interpreter) {
    interpreter.setLineNumber(token.getLineNumber());
    String eagerImage;
    if (token instanceof TagToken) {
      eagerImage = getEagerTagImage((TagToken) token, interpreter);
    } else {
      throw new DeferredValueException("Unsupported Token type");
    }
    return eagerImage;
  }

  /**
   * Uses the {@link EagerExpressionResolver} to partially evaluate any expression within
   * the tagToken's helpers. If there are any macro functions that must be deferred,
   * then their images are pre-pended to the result, which is the partial image
   * of the {@link TagToken}.
   * @param tagToken TagToken to get the eager image of.
   * @param interpreter The Jinjava interpreter.
   * @return A new image of the tagToken, which may have expressions that are further
   *  resolved than in the original {@link TagToken#getImage()}.
   */
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName());

    EagerExpressionResult eagerExpressionResult = EagerExpressionResolver.resolveExpression(
      tagToken.getHelpers().trim(),
      interpreter
    );
    String resolvedString = eagerExpressionResult.toString();
    if (StringUtils.isNotBlank(resolvedString)) {
      joiner.add(resolvedString);
    }
    joiner.add(tagToken.getSymbols().getExpressionEndWithTag());

    String prefixToPreserveState =
      EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
        eagerExpressionResult.getDeferredWords(),
        interpreter
      ) +
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        new DeferredToken(
          new TagToken(
            joiner.toString(),
            tagToken.getLineNumber(),
            tagToken.getStartPosition(),
            tagToken.getSymbols()
          ),
          eagerExpressionResult
            .getDeferredWords()
            .stream()
            .filter(
              word ->
                !(interpreter.getContext().get(word) instanceof DeferredMacroValueImpl)
            )
            .collect(Collectors.toSet())
        )
      );

    return (prefixToPreserveState + joiner.toString());
  }
}
