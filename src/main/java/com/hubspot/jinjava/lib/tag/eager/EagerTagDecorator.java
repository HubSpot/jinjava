package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.EagerValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import org.apache.commons.lang3.StringUtils;

public class EagerTagDecorator<T extends Tag> implements EagerTag {
  private T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  @Override
  public String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    StringBuilder result = new StringBuilder(tagNode.getMaster().getImage());

    for (Node child : tagNode.getChildren()) {
      result.append(interpreter.render(child));
    }

    if (StringUtils.isNotBlank(tagNode.getEndName())) {
      result.append("{% ").append(tagNode.getEndName()).append(" %}");
    }

    return result.toString();
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return tag.interpret(tagNode, interpreter);
    } catch (DeferredValueException e) {
      interpreter.getContext().handleDeferredNode(tagNode);
      return eagerInterpret(tagNode, interpreter);
    }
  }

  @Override
  public String getName() {
    return tag.getName();
  }
}
