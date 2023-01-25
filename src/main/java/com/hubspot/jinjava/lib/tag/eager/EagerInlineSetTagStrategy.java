package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Triple;

@Beta
public class EagerInlineSetTagStrategy extends EagerSetTagStrategy {
  public static final EagerInlineSetTagStrategy INSTANCE = new EagerInlineSetTagStrategy(
    new SetTag()
  );

  protected EagerInlineSetTagStrategy(SetTag setTag) {
    super(setTag);
  }

  @Override
  public EagerExecutionResult getEagerExecutionResult(
    TagNode tagNode,
    String expression,
    JinjavaInterpreter interpreter
  ) {
    return EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression('[' + expression + ']', interpreter),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withTakeNewValue(true)
        .build()
    );
  }

  @Override
  public Optional<String> resolveSet(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    try {
      setTag.executeSet(
        (TagToken) tagNode.getMaster(),
        interpreter,
        variables,
        eagerExecutionResult.getResult().toList(),
        true
      );
      return Optional.of("");
    } catch (DeferredValueException ignored) {}
    return Optional.empty();
  }

  @Override
  public Triple<String, String, String> getPrefixTokenAndSuffix(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    String deferredResult = eagerExecutionResult.getResult().toString();
    if (WhitespaceUtils.isWrappedWith(deferredResult, "[", "]")) {
      deferredResult = deferredResult.substring(1, deferredResult.length() - 1);
    }
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    )
      .add(tagNode.getSymbols().getExpressionStartWithTag())
      .add(tagNode.getTag().getName())
      .add(String.join(",", variables))
      .add("=")
      .add(deferredResult)
      .add(tagNode.getSymbols().getExpressionEndWithTag());
    String prefixToPreserveState =
      getPrefixToPreserveState(eagerExecutionResult, variables, interpreter) +
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        new DeferredToken(
          new TagToken(
            joiner.toString(),
            tagNode.getLineNumber(),
            tagNode.getStartPosition(),
            tagNode.getSymbols()
          ),
          eagerExecutionResult
            .getResult()
            .getDeferredWords()
            .stream()
            .filter(
              word ->
                !(interpreter.getContext().get(word) instanceof DeferredMacroValueImpl)
            )
            .collect(Collectors.toSet()),
          Arrays.stream(variables).map(String::trim).collect(Collectors.toSet())
        )
      );
    String suffixToPreserveState = getSuffixToPreserveState(
      String.join(",", Arrays.asList(variables)),
      interpreter
    );
    return Triple.of(prefixToPreserveState, joiner.toString(), suffixToPreserveState);
  }

  @Override
  public void attemptResolve(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    resolveSet(tagNode, variables, eagerExecutionResult, interpreter);
  }

  @Override
  public String buildImage(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    Triple<String, String, String> triple,
    JinjavaInterpreter interpreter
  ) {
    return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
      triple.getLeft() + triple.getMiddle() + triple.getRight(),
      interpreter
    );
  }
}
