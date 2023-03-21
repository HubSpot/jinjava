package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerStateChangingTag<T extends Tag> extends EagerTagDecorator<T> {

  public EagerStateChangingTag(T tag) {
    super(tag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return eagerInterpret(tagNode, interpreter, null);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    StringBuilder result = new StringBuilder(
      getEagerImage(
        buildToken(tagNode, e, interpreter.getLineNumber(), interpreter.getPosition()),
        interpreter
      )
    );

    if (!tagNode.getChildren().isEmpty()) {
      result.append(
        EagerContextWatcher
          .executeInChildContext(
            eagerInterpreter ->
              EagerExpressionResult.fromString(renderChildren(tagNode, eagerInterpreter)),
            interpreter,
            EagerContextWatcher
              .EagerChildContextConfig.newBuilder()
              .withForceDeferredExecutionMode(true)
              .build()
          )
          .asTemplateString()
      );
    }
    if (
      StringUtils.isNotBlank(tagNode.getEndName()) &&
      (
        !(getTag() instanceof FlexibleTag) ||
        ((FlexibleTag) getTag()).hasEndTag((TagToken) tagNode.getMaster())
      )
    ) {
      result.append(EagerReconstructionUtils.reconstructEnd(tagNode));
    }

    return result.toString();
  }
}
