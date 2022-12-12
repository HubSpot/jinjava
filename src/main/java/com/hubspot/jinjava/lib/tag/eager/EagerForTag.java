package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.EagerReconstructionUtils.EagerChildContextConfig;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Set<DeferredToken> addedTokens = new HashSet<>();
    EagerExecutionResult result = EagerReconstructionUtils.executeInChildContext(
      eagerInterpreter -> {
        EagerExpressionResult expressionResult = EagerExpressionResult.fromSupplier(
          () -> getTag().interpretUnchecked(tagNode, eagerInterpreter),
          eagerInterpreter
        );
        addedTokens.addAll(eagerInterpreter.getContext().getDeferredTokens());
        return expressionResult;
      },
      interpreter,
      EagerChildContextConfig.newBuilder().withCheckForContextChanges(true).build()
    );
    if (
      result.getResult().getResolutionState() == ResolutionState.NONE ||
      (
        !result.getResult().isFullyResolved() &&
        !result.getSpeculativeBindings().isEmpty()
      )
    ) {
      EagerIfTag.resetBindingsForNextBranch(interpreter, result);
      interpreter.getContext().removeDeferredTokens(addedTokens);
      throw new DeferredValueException(
        result.getResult().getResolutionState() == ResolutionState.NONE
          ? result.getResult().toString()
          : "Modification inside partially evaluated for loop"
      );
    }
    if (result.getResult().isFullyResolved()) {
      return result.getResult().toString(true);
    } else {
      return EagerReconstructionUtils.wrapInChildScope(
        result.getResult().toString(true),
        interpreter
      );
    }
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );
    String prefix = "";

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
        EagerReconstructionUtils
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
            EagerChildContextConfig.newBuilder().build()
          )
          .asTemplateString()
      );
    }

    EagerExecutionResult eagerExecutionResult = runLoopOnce(tagNode, interpreter);
    if (!eagerExecutionResult.getSpeculativeBindings().isEmpty()) {
      // Defer any variables that we tried to modify during the loop
      prefix = eagerExecutionResult.getPrefixToPreserveState(true);
    }
    // Run for loop again now that the necessary values have been deferred
    eagerExecutionResult = runLoopOnce(tagNode, interpreter);
    if (!eagerExecutionResult.getSpeculativeBindings().isEmpty()) {
      throw new DeferredValueException(
        "Modified values in deferred for loop: " +
        String.join(", ", eagerExecutionResult.getSpeculativeBindings().keySet())
      );
    }

    result.append(eagerExecutionResult.asTemplateString());
    result.append(EagerReconstructionUtils.reconstructEnd(tagNode));
    return prefix + result;
  }

  private EagerExecutionResult runLoopOnce(
    TagNode tagNode,
    JinjavaInterpreter interpreter
  ) {
    return EagerReconstructionUtils.executeInChildContext(
      eagerInterpreter -> {
        if (!(eagerInterpreter.getContext().get("loop") instanceof DeferredValue)) {
          eagerInterpreter.getContext().put("loop", DeferredValue.instance());
        }
        return EagerExpressionResult.fromString(
          renderChildren(tagNode, eagerInterpreter)
        );
      },
      interpreter,
      EagerChildContextConfig
        .newBuilder()
        .withForceDeferredExecutionMode(true)
        .withCheckForContextChanges(true)
        .build()
    );
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    Pair<List<String>, String> loopVarsAndExpression = getTag()
      .getLoopVarsAndExpression(tagToken);
    List<String> loopVars = loopVarsAndExpression.getLeft();
    String loopExpression = loopVarsAndExpression.getRight();

    EagerExpressionResult eagerExpressionResult = EagerExpressionResolver.resolveExpression(
      loopExpression,
      interpreter
    );

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
    String newlyDeferredFunctionImages = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
      eagerExpressionResult.getDeferredWords(),
      interpreter
    );
    EagerReconstructionUtils.removeMetaContextVariables(
      loopVars.stream(),
      interpreter.getContext()
    );

    interpreter
      .getContext()
      .handleDeferredToken(
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
            .collect(Collectors.toSet()),
          new HashSet<>(loopVars)
        )
      );
    return (newlyDeferredFunctionImages + joiner.toString());
  }
}
