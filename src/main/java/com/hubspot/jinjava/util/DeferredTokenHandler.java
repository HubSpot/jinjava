package com.hubspot.jinjava.util;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredLazyReference;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class DeferredTokenHandler {

  public static void deferPropertiesOnContext(
    Context context,
    DeferredToken deferredToken,
    Set<String> wordsWithoutDeferredSource
  ) {
    wordsWithoutDeferredSource.forEach(word -> deferDuplicatePointers(context, word));
    wordsWithoutDeferredSource.removeAll(
      markDeferredWordsAndFindSources(context, wordsWithoutDeferredSource)
    );

    if (isInSameScope(context, deferredToken)) {
      // set props are only deferred when within the scope which the variable is set in
      markDeferredWordsAndFindSources(context, deferredToken.getSetDeferredWords());
    }
  }

  // If 'list_a' and 'list_b' reference the same object, and 'list_a' is getting deferred, also defer 'list_b'
  private static void deferDuplicatePointers(Context context, String word) {
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

  private static Collection<String> markDeferredWordsAndFindSources(
    Context context,
    Set<String> wordsToDefer
  ) {
    return wordsToDefer
      .stream()
      .filter(prop -> !(context.get(prop) instanceof DeferredValue))
      .filter(prop -> !context.getMetaContextVariables().contains(prop))
      .filter(
        prop -> {
          DeferredValue deferredValue = convertToDeferredValue(context, prop);
          context.put(prop, deferredValue);
          return !(deferredValue instanceof DeferredValueShadow);
        }
      )
      .collect(Collectors.toList());
  }

  private static DeferredValue convertToDeferredValue(Context context, String prop) {
    Object valueInScope = context.getScope().get(prop);
    Object value = context.get(prop);
    if (value != null) {
      if (valueInScope == null) {
        return DeferredValue.shadowInstance(value);
      } else {
        return DeferredValue.instance(value);
      }
    }
    return DeferredValue.instance();
  }

  private static boolean isInSameScope(Context context, DeferredToken deferredToken) {
    return (
      deferredToken.getMacroStack() == null ||
      deferredToken.getMacroStack() == context.getMacroStack()
    );
  }
}
