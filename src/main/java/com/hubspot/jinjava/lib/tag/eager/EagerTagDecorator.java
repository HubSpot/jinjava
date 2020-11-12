package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.AutoEscapeTag;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;

public abstract class EagerTagDecorator<T extends Tag> implements Tag {
  private T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  public T getTag() {
    return tag;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return tag.interpret(tagNode, interpreter);
    } catch (DeferredValueException e) {
      if (interpreter.getConfig().isEagerExecutionEnabled()) {
        return wrapInAutoEscapeIfNeeded(
          eagerInterpret(tagNode, interpreter),
          interpreter
        );
      } else {
        throw e;
      }
    }
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

  public String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return ""; // TODO replace with actual functionality
  }

  public static String wrapInAutoEscapeIfNeeded(
    String output,
    JinjavaInterpreter interpreter
  ) {
    if (
      interpreter.getContext().isAutoEscape() &&
      (
        interpreter.getContext().getParent() == null ||
        !interpreter.getContext().getParent().isAutoEscape()
      )
    ) {
      output = wrapInTag(output, AutoEscapeTag.TAG_NAME, interpreter);
    }
    return output;
  }

  public static String wrapInTag(
    String s,
    String tagNameToWrap,
    JinjavaInterpreter interpreter
  ) {
    return (
      String.format(
        "%s %s %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        tagNameToWrap,
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      ) +
      s +
      String.format(
        "%s end%s %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        tagNameToWrap,
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      )
    );
  }
}
