package com.hubspot.jinjava.lib.tag.eager;

import static com.hubspot.jinjava.util.EagerReconstructionUtils.buildBlockSetTag;
import static com.hubspot.jinjava.util.EagerReconstructionUtils.buildSetTag;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.LazyReference;
import com.hubspot.jinjava.objects.serialization.PyishBlockSetSerializable;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This represents the result of speculatively executing an expression, where if something
 * got deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
@Beta
public class EagerExecutionResult {
  private final EagerExpressionResult result;
  private final Map<String, Object> speculativeBindings;
  private PrefixToPreserveState prefixToPreserveState;

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

  public PrefixToPreserveState getPrefixToPreserveState() {
    if (prefixToPreserveState != null) {
      return prefixToPreserveState;
    }
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    prefixToPreserveState = new PrefixToPreserveState();
    Collection<Entry<String, Object>> filteredEntries = speculativeBindings
      .entrySet()
      .stream()
      .filter(
        entry -> {
          Object contextValue = interpreter.getContext().get(entry.getKey());
          if (contextValue instanceof DeferredLazyReferenceSource) {
            ((DeferredLazyReferenceSource) contextValue).setReconstructed(true);
          }
          return !(contextValue instanceof DeferredValueShadow);
        }
      )
      .collect(Collectors.toList());
    prefixToPreserveState.putAll(
      filteredEntries
        .stream()
        .filter(entry -> entry.getValue() instanceof PyishBlockSetSerializable)
        .map(
          entry ->
            new AbstractMap.SimpleImmutableEntry<>(
              entry.getKey(),
              buildBlockSetTag(
                entry.getKey(),
                ((PyishBlockSetSerializable) entry.getValue()).getBlockSetBody(),
                interpreter,
                false
              )
            )
        )
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
    );
    filteredEntries
      .stream()
      .filter(entry -> !(entry.getValue() instanceof PyishBlockSetSerializable))
      .filter(entry -> !(entry.getValue() instanceof LazyReference))
      .forEach(
        entry ->
          prefixToPreserveState.put(
            entry.getKey(),
            buildSetTag(
              Collections.singletonMap(
                entry.getKey(),
                PyishObjectMapper.getAsPyishString(entry.getValue())
              ),
              interpreter,
              false
            )
          )
      );
    filteredEntries
      .stream()
      .filter(entry -> (entry.getValue() instanceof LazyReference))
      .map(
        entry ->
          new AbstractMap.SimpleImmutableEntry<>(
            entry.getKey(),
            PyishObjectMapper.getAsPyishString(entry.getValue())
          )
      )
      .sorted(
        (a, b) ->
          a.getValue().equals(b.getKey()) ? 1 : b.getValue().equals(a.getKey()) ? -1 : 0
      )
      .forEach(
        entry ->
          prefixToPreserveState.put(
            entry.getKey(),
            buildSetTag(
              Collections.singletonMap(entry.getKey(), entry.getValue()),
              interpreter,
              false
            )
          )
      );
    return prefixToPreserveState;
  }

  public String asTemplateString() {
    return getPrefixToPreserveState().toString() + result.toString(true);
  }
}
