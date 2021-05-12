package com.hubspot.jinjava.lib.tag.eager;

import static com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator.buildSetTagForDeferredInChildContext;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.Namespace;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This represents the result of speculatively executing an expression, where if something
 * got deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
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
    if (prefixToPreserveState != null) {
      return prefixToPreserveState;
    }
    prefixToPreserveState =
      buildSetTagForDeferredInChildContext(
        speculativeBindings
          .entrySet()
          .stream()
          .collect(
            Collectors.toMap(
              Entry::getKey,
              entry ->
                String.format(
                  entry.getValue() instanceof Namespace ? "namespace(%s)" : "%s",
                  PyishObjectMapper.getAsPyishString(entry.getValue())
                )
            )
          ),
        JinjavaInterpreter.getCurrent(),
        !JinjavaInterpreter
          .getCurrentMaybe()
          .map(interpreter -> interpreter.getContext().isDeferredExecutionMode())
          .orElse(false)
      );
    return prefixToPreserveState;
  }

  public String asTemplateString() {
    return getPrefixToPreserveState() + result;
  }
}
