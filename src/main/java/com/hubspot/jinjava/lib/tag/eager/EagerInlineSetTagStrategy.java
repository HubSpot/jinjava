package com.hubspot.jinjava.lib.tag.eager;


import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Arrays;
import java.util.Optional;
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
    String[] variables,
    String expression,
    JinjavaInterpreter interpreter
  ) {
    EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression('[' + expression + ']', interpreter),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withTakeNewValue(true)
        .build()
    );
    try {
      JinjavaInterpreter.checkOutputSize(result.getResult().toString());
    } catch (OutputTooBigException e) {
      ENGINE_LOG.error(
        "{} OutputTooBigException {} for: {}",
        getClass().getSimpleName(),
        e.getMessage(),
        expression
      );
    }

    return result;
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
    String deferredResult;
    try {
      deferredResult = eagerExecutionResult.getResult().toString();
    } catch (OutputTooBigException e) {
      ENGINE_LOG.error(
        "{} OutputTooBigException {} for: {}",
        getClass().getSimpleName(),
        e.getMessage(),
        tagNode.reconstructImage(),
        e
      );
      throw e;
    } catch (Exception e) {
      ENGINE_LOG.error(
        "{} Exception {} for: {}",
        getClass().getSimpleName(),
        e.getClass().getSimpleName(),
        tagNode.reconstructImage()
      );
      throw e;
    }
    if (deferredResult.length() > 10_000) {
      ENGINE_LOG.warn(
        "{} DeferredResult is too big for \"{}\". DeferredResult string size: {} {}",
        getClass().getSimpleName(),
        tagNode.reconstructImage(),
        deferredResult.length(),
        deferredResult.substring(0, 2000)
      );
    }
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
    PrefixToPreserveState prefixToPreserveState = getPrefixToPreserveState(
      eagerExecutionResult,
      variables,
      interpreter
    );
    prefixToPreserveState.withAllInFront(
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        DeferredToken
          .builderFromImage(joiner.toString(), tagNode.getMaster())
          .addUsedDeferredWords(eagerExecutionResult.getResult().getDeferredWords())
          .addSetDeferredWords(Arrays.stream(variables).map(String::trim))
          .build()
      )
    );
    String suffixToPreserveState = getSuffixToPreserveState(
      String.join(",", Arrays.asList(variables)),
      interpreter
    );
    return Triple.of(
      prefixToPreserveState.toString(),
      joiner.toString(),
      suffixToPreserveState
    );
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
