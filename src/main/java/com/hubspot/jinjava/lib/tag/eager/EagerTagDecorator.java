package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.EagerValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;

public abstract class EagerTagDecorator<T extends Tag> implements Tag {
  private T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  public abstract String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter);

  public abstract void handleEagerValueException(
    EagerValueException e,
    TagNode tagNode,
    JinjavaInterpreter interpreter
  );

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return tag.interpret(tagNode, interpreter);
    } catch (EagerValueException e) {
      return eagerInterpret(tagNode, interpreter);
    }
  }

  @Override
  public String getName() {
    return tag.getName();
  }
}
