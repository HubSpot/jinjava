package com.hubspot.jinjava.lib.tag.eager;

import static com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator.buildSetTagForDeferredInChildContext;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This represents the result of executing an expression, where if something got
 * deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
public class EagerStringResult {
  private final String result;
  private final Map<String, Object> sessionBindings;
  private String prefixToPreserveState;

  public EagerStringResult(String result, Map<String, Object> sessionBindings) {
    this.result = result;
    this.sessionBindings = sessionBindings;
  }

  public String getResult() {
    return result;
  }

  public Map<String, Object> getSessionBindings() {
    return sessionBindings;
  }

  public String getPrefixToPreserveState() {
    if (prefixToPreserveState != null) {
      return prefixToPreserveState;
    }
    prefixToPreserveState =
      buildSetTagForDeferredInChildContext(
        sessionBindings
          .entrySet()
          .stream()
          .collect(
            Collectors.toMap(
              Entry::getKey,
              entry -> PyishObjectMapper.getAsPyishString(entry.getValue())
            )
          ),
        JinjavaInterpreter.getCurrent(),
        false
      );
    return prefixToPreserveState;
  }

  public String asTemplateString() {
    return getPrefixToPreserveState() + result;
  }
}
