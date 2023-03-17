package com.hubspot.jinjava.util;

import com.google.common.collect.Sets;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredLazyReference;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EagerDeferredValueUtils {

  public static void findAndMarkDeferredPropertiesInToken(
    Context context,
    DeferredToken deferredToken
  ) {
    // set props are only deferred when within the scope which the variable is set in
    Set<String> setProps = new HashSet<>();
    Set<String> usedProps = new HashSet<>();
    if (isInSameScope(context, deferredToken)) {
      setProps.addAll(deferredToken.getSetDeferredWords());
      usedProps.addAll(
        deferredToken
          .getUntouchedUsedDeferredWords()
          .stream()
          .filter(context::containsKey)
          .collect(Collectors.toSet())
      );
    } else {
      List<String> macroArgs = deferredToken
        .getMacroStack()
        .peek()
        .map(
          name ->
            Optional
              .ofNullable(context.getGlobalMacro(name))
              .map(AbstractCallableMethod::getArguments)
              .orElseGet(
                () ->
                  context
                    .getLocalMacro(name)
                    .map(AbstractCallableMethod::getArguments)
                    .orElse(Collections.emptyList())
              )
        )
        .orElse(Collections.emptyList());
      // Filter out macro args because we will want them to be deferred on the higher-level contexts later
      usedProps.addAll(
        deferredToken
          .getUntouchedUsedDeferredWords()
          .stream()
          .filter(context::containsKey)
          .filter(prop -> !macroArgs.contains(prop))
          .collect(Collectors.toSet())
      );
    }
    usedProps.forEach(word -> findAndDeferDuplicatePointers(context, word));
    if (!usedProps.isEmpty()) {
      deferredToken
        .getUsedDeferredWords()
        .stream()
        .filter(
          key -> {
            Object val = context.getScope().get(key);
            return val != null && !(val instanceof DeferredValueShadow);
          }
        )
        .forEach(key -> deferredToken.getUntouchedUsedDeferredWords().remove(key));
    }

    markDeferredWords(context, Sets.union(setProps, usedProps));
  }

  // If 'list_a' and 'list_b' reference the same object, and 'list_a' is getting deferred, also defer 'list_b'
  private static void findAndDeferDuplicatePointers(Context context, String word) {
    Object wordValue = context.get(word);

    if (
      !(wordValue instanceof DeferredValue) &&
      !EagerExpressionResolver.isPrimitive(wordValue)
    ) {
      DeferredLazyReference deferredLazyReference = DeferredLazyReference.instance(
        context,
        word
      );
      Context temp = context;
      Set<Entry<String, Object>> matchingEntries = new HashSet<>();
      while (temp.getParent() != null) {
        temp
          .getScope()
          .entrySet()
          .stream()
          .filter(
            entry ->
              entry.getValue() == wordValue ||
              (
                entry.getValue() instanceof DeferredValue &&
                ((DeferredValue) entry.getValue()).getOriginalValue() == wordValue
              )
          )
          .forEach(
            entry -> {
              matchingEntries.add(entry);
              deferredLazyReference.getOriginalValue().setReferenceKey(entry.getKey());
            }
          );
        temp = temp.getParent();
      }
      if (matchingEntries.size() > 1) { // at least one duplicate
        matchingEntries.forEach(
          entry -> {
            if (
              deferredLazyReference
                .getOriginalValue()
                .getReferenceKey()
                .equals(entry.getKey())
            ) {
              convertToDeferredLazyReferenceSource(context, entry);
            } else {
              entry.setValue(deferredLazyReference);
            }
          }
        );
      }
    }
  }

  private static void convertToDeferredLazyReferenceSource(
    Context context,
    Entry<String, Object> entry
  ) {
    Object val = entry.getValue();
    if (val instanceof DeferredLazyReferenceSource) {
      return;
    }
    DeferredLazyReferenceSource deferredLazyReferenceSource = DeferredLazyReferenceSource.instance(
      val instanceof DeferredValue ? ((DeferredValue) val).getOriginalValue() : val
    );

    context.replace(entry.getKey(), deferredLazyReferenceSource);
    entry.setValue(deferredLazyReferenceSource);
  }

  private static void markDeferredWords(Context context, Set<String> wordsToDefer) {
    wordsToDefer
      .stream()
      .filter(prop -> !(context.get(prop) instanceof DeferredValue))
      .filter(prop -> !context.getMetaContextVariables().contains(prop))
      .forEach(prop -> context.put(prop, convertToDeferredValue(context, prop)));
  }

  private static DeferredValue convertToDeferredValue(Context context, String prop) {
    DeferredValue deferredValue = DeferredValue.instance();
    Object valueInScope = context.getScope().get(prop);
    Object value = context.get(prop);
    if (value != null) {
      if (valueInScope == null) {
        deferredValue = DeferredValue.shadowInstance(value);
      } else {
        deferredValue = DeferredValue.instance(value);
      }
    }
    return deferredValue;
  }

  private static boolean isInSameScope(Context context, DeferredToken deferredToken) {
    return (
      deferredToken.getMacroStack() == null ||
      deferredToken.getMacroStack() == context.getMacroStack()
    );
  }
}
