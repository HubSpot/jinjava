package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.expression.EagerExpressionStrategy;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.CallTag;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerCallTag extends EagerStateChangingTag<CallTag> {

  public EagerCallTag() {
    super(new CallTag());
  }

  public EagerCallTag(CallTag tag) {
    super(tag);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    interpreter.getContext().checkNumberOfDeferredTokens();
    MacroFunction caller;
    EagerExecutionResult eagerExecutionResult;
    PrefixToPreserveState prefixToPreserveState;
    LengthLimitingStringJoiner joiner;
    try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
      caller =
        new EagerMacroFunction(
          tagNode.getChildren(),
          "caller",
          new LinkedHashMap<>(),
          true,
          interpreter.getContext(),
          interpreter.getLineNumber(),
          interpreter.getPosition()
        );
      interpreter.getContext().addGlobalMacro(caller);
      eagerExecutionResult =
        EagerContextWatcher.executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResolver.resolveExpression(
              tagNode.getHelpers().trim(),
              interpreter
            ),
          interpreter,
          EagerContextWatcher.EagerChildContextConfig
            .newBuilder()
            .withTakeNewValue(true)
            .withPartialMacroEvaluation(
              interpreter.getConfig().isNestedInterpretationEnabled()
            )
            .build()
        );
      prefixToPreserveState = new PrefixToPreserveState();
      if (
        !eagerExecutionResult.getResult().isFullyResolved() ||
        interpreter.getContext().isDeferredExecutionMode()
      ) {
        prefixToPreserveState.putAll(eagerExecutionResult.getPrefixToPreserveState());
      } else {
        EagerReconstructionUtils.commitSpeculativeBindings(
          interpreter,
          eagerExecutionResult
        );
      }
      if (eagerExecutionResult.getResult().isFullyResolved()) {
        // Possible macro/set tag in front of this one.
        return (
          prefixToPreserveState.toString() +
          EagerExpressionStrategy.postProcessResult(
            new ExpressionToken(
              tagNode.getHelpers(),
              tagNode.getLineNumber(),
              tagNode.getStartPosition(),
              tagNode.getSymbols()
            ),
            eagerExecutionResult.getResult().toString(true),
            interpreter
          )
        );
      }

      caller.setDeferred(true);
      // caller() needs to exist here so that the macro function can be reconstructed
      EagerReconstructionUtils.hydrateReconstructionFromContextBeforeDeferring(
        prefixToPreserveState,
        eagerExecutionResult.getResult().getDeferredWords(),
        interpreter
      );
    }

    // Now preserve those variables from the scope the call tag was called in
    prefixToPreserveState.withAllInFront(
      new EagerExecutionResult(
        eagerExecutionResult.getResult(),
        eagerExecutionResult.getSpeculativeBindings()
      )
        .getPrefixToPreserveState()
    );
    joiner =
      new LengthLimitingStringJoiner(interpreter.getConfig().getMaxOutputSize(), " ");
    joiner
      .add(tagNode.getSymbols().getExpressionStartWithTag())
      .add(tagNode.getTag().getName())
      .add(eagerExecutionResult.getResult().toString().trim())
      .add(tagNode.getSymbols().getExpressionEndWithTag());
    prefixToPreserveState.withAllInFront(
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        DeferredToken
          .builderFromImage(joiner.toString(), tagNode.getMaster())
          .addUsedDeferredWords(eagerExecutionResult.getResult().getDeferredWords())
          .build()
      )
    );

    StringBuilder result = new StringBuilder(prefixToPreserveState + joiner.toString());
    interpreter.getContext().setDynamicVariableResolver(s -> DeferredValue.instance());
    if (!tagNode.getChildren().isEmpty()) {
      result.append(
        EagerContextWatcher
          .executeInChildContext(
            eagerInterpreter ->
              EagerExpressionResult.fromString(renderChildren(tagNode, eagerInterpreter)),
            interpreter,
            EagerContextWatcher.EagerChildContextConfig
              .newBuilder()
              .withForceDeferredExecutionMode(true)
              .build()
          )
          .asTemplateString()
      );
    }
    if (
      StringUtils.isNotBlank(tagNode.getEndName()) &&
      (!(getTag() instanceof FlexibleTag) ||
        ((FlexibleTag) getTag()).hasEndTag((TagToken) tagNode.getMaster()))
    ) {
      result.append(EagerReconstructionUtils.reconstructEnd(tagNode));
    } // Possible set tag in front of this one.
    return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
      result.toString(),
      interpreter
    );
  }
}
