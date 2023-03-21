package com.hubspot.jinjava.util;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.CannotReconstructValueException;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.LazyExpression;
import com.hubspot.jinjava.interpret.RevertibleObject;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Beta
public class EagerContextWatcher {

  /**
   * Execute the specified functions within a protected context.
   * Additionally, if the execution causes existing values on the context to become
   *   deferred, then their previous values will wrapped in a <code>set</code>
   *   tag that gets prepended to the returned result.
   * The <code>function</code> is run in deferredExecutionMode=true, where the context needs to
   *   be protected from having values updated or set,
   *   such as when evaluating both the positive and negative nodes in an if statement.
   * @param function Function to run within a "protected" child context
   * @param interpreter JinjavaInterpreter to create a child from.
   * @param eagerChildContextConfig Configuration for evaluation as defined in {@link EagerChildContextConfig}
   * @return An <code>EagerExecutionResult</code> where:
   *  <code>result</code> is the string result of <code>function</code>.
   *  <code>prefixToPreserveState</code> is either blank or a <code>set</code> tag
   *    that preserves the state within the output for a second rendering pass.
   */
  public static EagerExecutionResult executeInChildContext(
    Function<JinjavaInterpreter, EagerExpressionResult> function,
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig
  ) {
    final Set<String> metaContextVariables = interpreter
      .getContext()
      .getMetaContextVariables();
    final EagerExecutionResult initialResult;
    final Map<String, Object> speculativeBindings;
    if (eagerChildContextConfig.checkForContextChanges) {
      final Set<Entry<String, Object>> entrySet = interpreter.getContext().entrySet();
      final Map<String, Object> initiallyResolvedHashes = getInitiallyResolvedHashes(
        entrySet,
        metaContextVariables
      );
      final Map<String, String> initiallyResolvedAsStrings = getInitiallyResolvedAsStrings(
        interpreter,
        entrySet,
        initiallyResolvedHashes
      );
      initialResult = applyFunction(function, interpreter, eagerChildContextConfig);
      speculativeBindings =
        getAllSpeculativeBindings(
          interpreter,
          eagerChildContextConfig,
          metaContextVariables,
          initiallyResolvedHashes,
          initiallyResolvedAsStrings,
          initialResult
        );
    } else {
      Set<String> ignoredKeys = getKeysToIgnore(
        interpreter,
        metaContextVariables,
        eagerChildContextConfig
      );
      initialResult = applyFunction(function, interpreter, eagerChildContextConfig);
      speculativeBindings =
        getBasicSpeculativeBindings(
          interpreter,
          eagerChildContextConfig,
          ignoredKeys,
          initialResult
        );
    }
    return new EagerExecutionResult(initialResult.getResult(), speculativeBindings);
  }

  private static EagerExecutionResult applyFunction(
    Function<JinjavaInterpreter, EagerExpressionResult> function,
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig
  ) {
    // Don't create new call stacks to prevent hitting max recursion with this silent new scope
    try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
      if (eagerChildContextConfig.forceDeferredExecutionMode) {
        interpreter.getContext().setDeferredExecutionMode(true);
      }
      interpreter
        .getContext()
        .setPartialMacroEvaluation(eagerChildContextConfig.partialMacroEvaluation);
      return new EagerExecutionResult(
        function.apply(interpreter),
        eagerChildContextConfig.discardSessionBindings
          ? new HashMap<>()
          : interpreter.getContext().getSessionBindings()
      );
    }
  }

  private static Map<String, String> getInitiallyResolvedAsStrings(
    JinjavaInterpreter interpreter,
    Set<Entry<String, Object>> entrySet,
    Map<String, Object> initiallyResolvedHashes
  ) {
    Map<String, String> initiallyResolvedAsStrings = new HashMap<>();
    // This creates a stringified snapshot of the context
    // so it can be disabled via the config because it may cause performance issues.
    Stream<Entry<String, Object>> entryStream =
      (
        interpreter.getConfig().getExecutionMode().useEagerContextReverting()
          ? entrySet
          : interpreter.getContext().getCombinedScope().entrySet()
      ).stream()
        .filter(entry -> initiallyResolvedHashes.containsKey(entry.getKey()))
        .filter(
          entry -> EagerExpressionResolver.isResolvableObject(entry.getValue(), 4, 400) // TODO make this configurable
        );
    entryStream.forEach(
      entry ->
        cacheRevertibleObject(
          interpreter,
          initiallyResolvedHashes,
          initiallyResolvedAsStrings,
          entry
        )
    );
    return initiallyResolvedAsStrings;
  }

  private static Map<String, Object> getInitiallyResolvedHashes(
    Set<Entry<String, Object>> entrySet,
    Set<String> metaContextVariables
  ) {
    Map<String, Object> mapOfHashes = new HashMap<>();
    entrySet
      .stream()
      .filter(entry -> !metaContextVariables.contains(entry.getKey()))
      .filter(
        entry -> !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
      )
      .forEach(
        entry -> mapOfHashes.put(entry.getKey(), getObjectOrHashCode(entry.getValue()))
      ); // Avoid NPE when getObjectOrHashCode(entry.getValue()) is null)
    return mapOfHashes;
  }

  private static Set<String> getKeysToIgnore(
    JinjavaInterpreter interpreter,
    Set<String> metaContextVariables,
    EagerChildContextConfig eagerChildContextConfig
  ) {
    // We don't need to reconstruct already deferred keys.
    // This ternary expression is an optimization to call entrySet fewer times
    return (
        interpreter.getContext().isDeferredExecutionMode() &&
        !eagerChildContextConfig.takeNewValue
      )
      ? Stream
        .concat(
          metaContextVariables.stream(),
          interpreter
            .getContext()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof DeferredValue)
            .map(Entry::getKey)
        )
        .collect(Collectors.toSet())
      : metaContextVariables;
  }

  private static Map<String, Object> getBasicSpeculativeBindings(
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig,
    Set<String> ignoredKeys,
    EagerExecutionResult eagerExecutionResult
  ) {
    eagerExecutionResult
      .getSpeculativeBindings()
      .putAll(
        interpreter
          .getContext()
          .getScope()
          .entrySet()
          .stream()
          .filter(
            entry ->
              entry.getValue() instanceof DeferredLazyReferenceSource &&
              !(((DeferredLazyReferenceSource) entry.getValue()).isReconstructed())
          )
          .peek(
            entry ->
              ((DeferredLazyReferenceSource) entry.getValue()).setReconstructed(true)
          )
          .collect(
            Collectors.toMap(
              Entry::getKey,
              entry -> ((DeferredLazyReferenceSource) entry.getValue()).getOriginalValue()
            )
          )
      );
    return eagerExecutionResult
      .getSpeculativeBindings()
      .entrySet()
      .stream()
      .filter(entry -> !ignoredKeys.contains(entry.getKey()))
      .filter(entry -> !"loop".equals(entry.getKey()))
      .map(
        entry -> {
          if (
            (
              eagerExecutionResult.getResult().isFullyResolved() ||
              eagerChildContextConfig.takeNewValue
            ) &&
            !(entry.getValue() instanceof DeferredValue) &&
            entry.getValue() != null
          ) {
            return entry;
          }
          Object contextValue = interpreter.getContext().get(entry.getKey());
          if (
            contextValue instanceof DeferredValue &&
            ((DeferredValue) contextValue).getOriginalValue() != null
          ) {
            if (
              !eagerChildContextConfig.takeNewValue &&
              !EagerExpressionResolver.isResolvableObject(
                ((DeferredValue) contextValue).getOriginalValue()
              )
            ) {
              throw new CannotReconstructValueException(entry.getKey());
            }
            return new AbstractMap.SimpleImmutableEntry<>(
              entry.getKey(),
              ((DeferredValue) contextValue).getOriginalValue()
            );
          }
          return null;
        }
      )
      .filter(Objects::nonNull)
      .collect(
        Collectors.toMap(
          Entry::getKey,
          entry ->
            entry.getValue() instanceof DeferredValue
              ? ((DeferredValue) entry.getValue()).getOriginalValue()
              : entry.getValue()
        )
      );
  }

  private static Map<String, Object> getAllSpeculativeBindings(
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig,
    Set<String> metaContextVariables,
    Map<String, Object> initiallyResolvedHashes,
    Map<String, String> initiallyResolvedAsStrings,
    EagerExecutionResult eagerExecutionResult
  ) {
    Map<String, Object> speculativeBindings = eagerExecutionResult
      .getSpeculativeBindings()
      .entrySet()
      .stream()
      .filter(
        entry ->
          entry.getValue() != null &&
          !entry.getValue().equals(interpreter.getContext().get(entry.getKey()))
      )
      .filter(
        entry -> !(interpreter.getContext().get(entry.getKey()) instanceof DeferredValue)
      )
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    speculativeBindings.putAll(
      initiallyResolvedHashes
        .keySet()
        .stream()
        .map(
          key ->
            new AbstractMap.SimpleImmutableEntry<>(key, interpreter.getContext().get(key))
        )
        .filter(
          entry ->
            !Objects.equals(
              initiallyResolvedHashes.get(entry.getKey()),
              getObjectOrHashCode(entry.getValue())
            )
        )
        .collect(
          Collectors.toMap(
            Entry::getKey,
            entry ->
              getOriginalValue(
                interpreter,
                eagerChildContextConfig,
                initiallyResolvedHashes,
                initiallyResolvedAsStrings,
                entry,
                eagerExecutionResult.getResult().isFullyResolved()
              )
          )
        )
    );

    speculativeBindings =
      speculativeBindings
        .entrySet()
        .stream()
        .filter(entry -> !metaContextVariables.contains(entry.getKey()))
        .filter(
          entry ->
            !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
        ) // these are already set recursively
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return speculativeBindings;
  }

  private static void cacheRevertibleObject(
    JinjavaInterpreter interpreter,
    Map<String, Object> initiallyResolvedHashes,
    Map<String, String> initiallyResolvedAsStrings,
    Entry<String, Object> entry
  ) {
    RevertibleObject revertibleObject = interpreter
      .getRevertibleObjects()
      .get(entry.getKey());
    Object hashCode = initiallyResolvedHashes.get(entry.getKey());
    try {
      if (revertibleObject == null || !hashCode.equals(revertibleObject.getHashCode())) {
        revertibleObject =
          new RevertibleObject(
            hashCode,
            PyishObjectMapper.getAsPyishStringOrThrow(entry.getValue())
          );
        interpreter.getRevertibleObjects().put(entry.getKey(), revertibleObject);
      }
      revertibleObject
        .getPyishString()
        .ifPresent(
          pyishString -> initiallyResolvedAsStrings.put(entry.getKey(), pyishString)
        );
    } catch (Exception e) {
      interpreter
        .getRevertibleObjects()
        .put(entry.getKey(), new RevertibleObject(hashCode));
    }
  }

  private static Object getOriginalValue(
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig,
    Map<String, Object> initiallyResolvedHashes,
    Map<String, String> initiallyResolvedAsStrings,
    Entry<String, Object> e,
    boolean isFullyResolved
  ) {
    if (eagerChildContextConfig.takeNewValue || isFullyResolved) {
      if (e.getValue() instanceof DeferredValue) {
        return ((DeferredValue) e.getValue()).getOriginalValue();
      }
      return e.getValue();
    }

    if (
      e.getValue() instanceof DeferredValue &&
      initiallyResolvedHashes
        .get(e.getKey())
        .equals(getObjectOrHashCode(((DeferredValue) e.getValue()).getOriginalValue()))
    ) {
      return ((DeferredValue) e.getValue()).getOriginalValue();
    }

    // This is necessary if a state-changing function, such as .update()
    // or .append() is run against a variable in the context.
    // It will revert the effects when takeNewValue is false.
    if (initiallyResolvedAsStrings.containsKey(e.getKey())) {
      // convert to new list or map
      try {
        return interpreter.resolveELExpression(
          initiallyResolvedAsStrings.get(e.getKey()),
          interpreter.getLineNumber()
        );
      } catch (DeferredValueException ignored) {}
    }

    // Previous value could not be mapped to a string
    throw new CannotReconstructValueException(e.getKey());
  }

  private static Object getObjectOrHashCode(Object o) {
    if (o instanceof LazyExpression) {
      o = ((LazyExpression) o).get();
    }

    if (o instanceof PyList && !((PyList) o).toList().contains(o)) {
      return o.hashCode();
    }
    if (o instanceof PyMap && !((PyMap) o).toMap().containsValue(o)) {
      return o.hashCode() + ((PyMap) o).keySet().hashCode();
    }
    return o;
  }

  public static class EagerChildContextConfig {
    private final boolean takeNewValue;

    private final boolean discardSessionBindings;
    private final boolean partialMacroEvaluation;

    private final boolean checkForContextChanges;
    private final boolean forceDeferredExecutionMode;

    private EagerChildContextConfig(
      boolean takeNewValue,
      boolean discardSessionBindings,
      boolean partialMacroEvaluation,
      boolean checkForContextChanges,
      boolean forceDeferredExecutionMode
    ) {
      this.takeNewValue = takeNewValue;
      this.discardSessionBindings = discardSessionBindings;
      this.partialMacroEvaluation = partialMacroEvaluation;
      this.checkForContextChanges = checkForContextChanges;
      this.forceDeferredExecutionMode = forceDeferredExecutionMode;
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static class Builder {
      private boolean takeNewValue;

      private boolean discardSessionBindings;
      private boolean partialMacroEvaluation;
      private boolean checkForContextChanges;
      private boolean forceDeferredExecutionMode;

      private Builder() {}

      /**
       * @param takeNewValue If a value is updated (not replaced) either take the new value or
       *                     take the previous value and put it into the
       *                     <code>EagerExecutionResult.prefixToPreserveState</code>.
       */
      public Builder withTakeNewValue(boolean takeNewValue) {
        this.takeNewValue = takeNewValue;
        return this;
      }

      /**
       * @param discardSessionBindings Discard the session bindings from the child context
       *                               created while executing the provided function.
       */
      public Builder withDiscardSessionBindings(boolean discardSessionBindings) {
        this.discardSessionBindings = discardSessionBindings;
        return this;
      }

      /**
       * @param partialMacroEvaluation Allow macro functions to be partially evaluated rather than
       *                               needing an explicit result during this render.
       */
      public Builder withPartialMacroEvaluation(boolean partialMacroEvaluation) {
        this.partialMacroEvaluation = partialMacroEvaluation;
        return this;
      }

      /**
       * @param checkForContextChanges Hash and serialize values on the context to determine if changes
       *                               have been made to any values on the context.
       */
      public Builder withCheckForContextChanges(boolean checkForContextChanges) {
        this.checkForContextChanges = checkForContextChanges;
        return this;
      }

      /**
       * @param forceDeferredExecutionMode Start the evaluation of the specified function in deferred execution mode.
       */
      public Builder withForceDeferredExecutionMode(boolean forceDeferredExecutionMode) {
        this.forceDeferredExecutionMode = forceDeferredExecutionMode;
        return this;
      }

      public EagerChildContextConfig build() {
        return new EagerChildContextConfig(
          takeNewValue,
          discardSessionBindings,
          partialMacroEvaluation,
          checkForContextChanges,
          forceDeferredExecutionMode
        );
      }
    }
  }
}
