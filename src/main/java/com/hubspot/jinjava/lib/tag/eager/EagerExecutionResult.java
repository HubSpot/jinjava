package com.hubspot.jinjava.lib.tag.eager;

import static com.hubspot.jinjava.util.EagerReconstructionUtils.buildBlockSetTag;
import static com.hubspot.jinjava.util.EagerReconstructionUtils.buildSetTag;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.LazyReference;
import com.hubspot.jinjava.objects.serialization.PyishBlockSetSerializable;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This represents the result of speculatively executing an expression, where if something
 * got deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
@Beta
public class EagerExecutionResult {
  private final EagerExpressionResult result;
  private final Map<String, Object> speculativeBindings;
  private String prefixToPreserveState;

  public EagerExecutionResult(
    EagerExpressionResult result,
    Map<String, Object> speculativeBindings
  ) {
    this.result = result;
    this.speculativeBindings = speculativeBindings;
  }

  public EagerExpressionResult getResult() {
    return result;
  }

  public Map<String, Object> getSpeculativeBindings() {
    return speculativeBindings;
  }

  public String getPrefixToPreserveState() {
    return getPrefixToPreserveState(
      !JinjavaInterpreter
        .getCurrentMaybe()
        .map(interpreter -> interpreter.getContext().isDeferredExecutionMode())
        .orElse(false)
    );
  }

  public String getPrefixToPreserveState(boolean registerDeferredToken) {
    if (prefixToPreserveState != null) {
      return prefixToPreserveState;
    }
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    prefixToPreserveState =
      speculativeBindings
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() instanceof PyishBlockSetSerializable)
        .map(
          entry ->
            buildBlockSetTag(
              entry.getKey(),
              ((PyishBlockSetSerializable) entry.getValue()).getBlockSetBody(),
              interpreter,
              registerDeferredToken
            )
        )
        .collect(Collectors.joining()) +
      buildSetTag(
        speculativeBindings
          .entrySet()
          .stream()
          .filter(entry -> !(entry.getValue() instanceof PyishBlockSetSerializable))
          .filter(entry -> !(entry.getValue() instanceof LazyReference))
          .collect(
            Collectors.toMap(
              Entry::getKey,
              entry -> PyishObjectMapper.getAsPyishString(entry.getValue())
            )
          ),
        interpreter,
        registerDeferredToken
      ) +
      speculativeBindings
        .entrySet()
        .stream()
        .filter(entry -> (entry.getValue() instanceof LazyReference))
        .map(
          entry ->
            Pair.of(entry.getKey(), PyishObjectMapper.getAsPyishString(entry.getValue()))
        )
        .sorted(
          (a, b) ->
            a.getValue().equals(b.getKey()) ? 1 : b.getValue().equals(a.getKey()) ? -1 : 0
        )
        .map(
          pair ->
            buildSetTag(
              Collections.singletonMap(pair.getKey(), pair.getValue()),
              interpreter,
              registerDeferredToken
            )
        )
        .collect(Collectors.joining());
    return prefixToPreserveState;
  }

  public String asTemplateString() {
    return getPrefixToPreserveState() + result;
  }
}
