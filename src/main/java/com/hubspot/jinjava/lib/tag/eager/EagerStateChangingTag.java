package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import org.apache.commons.lang3.StringUtils;

public class EagerStateChangingTag<T extends Tag> extends EagerTagDecorator<T> {

  public EagerStateChangingTag(T tag) {
    super(tag);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return eagerInterpret(tagNode, interpreter);
  }

  @Override
  public String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    StringBuilder result = new StringBuilder(
      getEagerImage(tagNode.getMaster(), interpreter)
    );

    // Currently always false
    if (!tagNode.getChildren().isEmpty()) {
      result.append(
        executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(renderChildren(tagNode, eagerInterpreter)),
          interpreter,
          false,
          false,
          true
        )
      );
    }

    // Currently always false
    if (StringUtils.isNotBlank(tagNode.getEndName())) {
      result.append(reconstructEnd(tagNode));
    }

    return result.toString();
  }
}
