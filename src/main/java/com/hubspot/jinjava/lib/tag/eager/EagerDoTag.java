package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;

public class EagerDoTag extends EagerStateChangingTag<DoTag> implements FlexibleTag {

  public EagerDoTag() {
    super(new DoTag());
  }

  public EagerDoTag(DoTag doTag) {
    super(doTag);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (hasEndTag((TagToken) tagNode.getMaster())) {
      EagerExecutionResult eagerExecutionResult = EagerContextWatcher.executeInChildContext(
        eagerInterpreter ->
          EagerExpressionResult.fromSupplier(
            () -> renderChildren(tagNode, interpreter),
            eagerInterpreter
          ),
        interpreter,
        EagerContextWatcher
          .EagerChildContextConfig.newBuilder()
          .withTakeNewValue(true)
          .withCheckForContextChanges(!interpreter.getContext().isDeferredExecutionMode())
          .build()
      );
      StringBuilder prefixToPreserveState = new StringBuilder();
      if (interpreter.getContext().isDeferredExecutionMode()) {
        prefixToPreserveState.append(eagerExecutionResult.getPrefixToPreserveState());
      } else {
        interpreter.getContext().putAll(eagerExecutionResult.getSpeculativeBindings());
      }
      if (eagerExecutionResult.getResult().isFullyResolved()) {
        return (prefixToPreserveState.toString());
      }
      return EagerReconstructionUtils.wrapInTag(
        eagerExecutionResult.asTemplateString(),
        getName(),
        interpreter,
        true
      );
    }
    return EagerPrintTag.interpretExpression(
      tagNode.getHelpers(),
      (TagToken) tagNode.getMaster(),
      interpreter,
      false
    );
  }

  @Override
  public boolean hasEndTag(TagToken tagToken) {
    return getTag().hasEndTag(tagToken);
  }
}
