package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.CannotReconstructValueException;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

@Beta
public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Pair<List<String>, String> loopVarsAndExpression = getTag()
      .getLoopVarsAndExpression((TagToken) tagNode.getMaster());
    EagerExecutionResult collectionResult = EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression(
          '[' + loopVarsAndExpression.getRight() + ']',
          interpreter
        ),
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withCheckForContextChanges(!interpreter.getContext().isDeferredExecutionMode())
        .build()
    );
    if (collectionResult.getResult().isFullyResolved()) {
      Set<DeferredToken> addedTokens = new HashSet<>();
      EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
        eagerInterpreter -> {
          EagerExpressionResult expressionResult = EagerExpressionResult.fromSupplier(
            () -> {
              try {
                interpreter
                  .getContext()
                  .addNonMetaContextVariables(loopVarsAndExpression.getLeft());
                return getTag()
                  .renderForCollection(
                    tagNode,
                    eagerInterpreter,
                    loopVarsAndExpression.getLeft(),
                    !collectionResult.getResult().toList().isEmpty()
                      ? collectionResult.getResult().toList().get(0)
                      : Collections.emptyList()
                  );
              } finally {
                interpreter
                  .getContext()
                  .removeNonMetaContextVariables(loopVarsAndExpression.getLeft());
              }
            },
            eagerInterpreter
          );
          addedTokens.addAll(eagerInterpreter.getContext().getDeferredTokens());
          return expressionResult;
        },
        interpreter,
        EagerContextWatcher.EagerChildContextConfig.newBuilder().build()
      );
      if (result.getResult().getResolutionState() == ResolutionState.NONE) {
        EagerReconstructionUtils.resetSpeculativeBindings(interpreter, collectionResult);
        EagerReconstructionUtils.resetSpeculativeBindings(interpreter, result);
        interpreter.getContext().removeDeferredTokens(addedTokens);
        throw new DeferredValueException(result.getResult().toString(true));
      }
      if (result.getResult().isFullyResolved()) {
        return result.getResult().toString(true);
      } else {
        return (
          result
            .getPrefixToPreserveState()
            .withAllInFront(collectionResult.getPrefixToPreserveState()) +
          EagerReconstructionUtils.wrapInChildScope(
            result.getResult().toString(true),
            interpreter
          )
        );
      }
    }
    EagerReconstructionUtils.resetSpeculativeBindings(interpreter, collectionResult);
    throw new DeferredValueException(collectionResult.getResult().toString(true));
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (e instanceof CannotReconstructValueException) {
      throw e;
    }
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    try (
      TemporaryValueClosable<Boolean> c = interpreter
        .getContext()
        .withDeferLargeObjects(
          ForTag.TOO_LARGE_EXCEPTION_MESSAGE.equals(e.getMessage()) ||
          interpreter.getContext().isDeferLargeObjects()
        )
    ) {
      // separate getEagerImage from renderChildren because the token gets evaluated once
      // while the children are evaluated 0...n times.
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
                )
              ),
            interpreter,
            EagerContextWatcher.EagerChildContextConfig.newBuilder().build()
          )
          .asTemplateString()
      );
    }

    EagerExecutionResult firstRunResult = runLoopOnce(tagNode, interpreter, true);
    PrefixToPreserveState prefixToPreserveState = firstRunResult
      .getPrefixToPreserveState()
      .withAllInFront(
        EagerReconstructionUtils.resetAndDeferSpeculativeBindings(
          interpreter,
          firstRunResult
        )
      );
    // Run for loop again now that the necessary values have been deferred
    EagerExecutionResult secondRunResult = runLoopOnce(tagNode, interpreter, false);
    if (
      secondRunResult
        .getSpeculativeBindings()
        .keySet()
        .stream()
        .anyMatch(key -> !firstRunResult.getSpeculativeBindings().containsKey(key))
    ) {
      throw new DeferredValueException(
        "Modified values in deferred for loop: " +
        String.join(", ", secondRunResult.getSpeculativeBindings().keySet())
      );
    }

    result.append(secondRunResult.asTemplateString());
    result.append(EagerReconstructionUtils.reconstructEnd(tagNode));
    return prefixToPreserveState.toString() + result;
  }

  private EagerExecutionResult runLoopOnce(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    boolean clearDeferredWords
  ) {
    return EagerContextWatcher.executeInChildContext(
      eagerInterpreter -> {
        if (!(eagerInterpreter.getContext().get(ForTag.LOOP) instanceof DeferredValue)) {
          eagerInterpreter.getContext().put(ForTag.LOOP, DeferredValue.instance());
        }
        List<String> loopVars = getTag()
          .getLoopVarsAndExpression((TagToken) tagNode.getMaster())
          .getLeft();
        interpreter.getContext().addNonMetaContextVariables(loopVars);
        loopVars.forEach(var ->
          interpreter.getContext().put(var, DeferredValue.instance())
        );
        try {
          return EagerExpressionResult.fromString(
            renderChildren(tagNode, eagerInterpreter)
          );
        } finally {
          interpreter.getContext().removeNonMetaContextVariables(loopVars);
          if (clearDeferredWords) {
            interpreter
              .getContext()
              .removeDeferredTokens(interpreter.getContext().getDeferredTokens());
          }
        }
      },
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withForceDeferredExecutionMode(true)
        .build()
    );
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    Pair<List<String>, String> loopVarsAndExpression = getTag()
      .getLoopVarsAndExpression(tagToken);
    List<String> loopVars = loopVarsAndExpression.getLeft();
    String loopExpression = loopVarsAndExpression.getRight();

    EagerExpressionResult eagerExpressionResult =
      EagerExpressionResolver.resolveExpression(loopExpression, interpreter);

    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );

    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(String.join(", ", loopVars))
      .add("in")
      .add(eagerExpressionResult.toString())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    PrefixToPreserveState prefixToPreserveState =
      EagerReconstructionUtils.hydrateReconstructionFromContextBeforeDeferring(
        new PrefixToPreserveState(),
        eagerExpressionResult.getDeferredWords(),
        interpreter
      );
    prefixToPreserveState.withAllInFront(
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        DeferredToken
          .builderFromImage(joiner.toString(), tagToken)
          .addUsedDeferredWords(eagerExpressionResult.getDeferredWords())
          .build()
      )
    );
    return (prefixToPreserveState + joiner.toString());
  }
}
