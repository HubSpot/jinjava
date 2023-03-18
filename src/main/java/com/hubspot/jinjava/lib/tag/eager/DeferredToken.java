package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.CallStack;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredLazyReference;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class DeferredToken {
  private final Token token;
  // These words aren't yet DeferredValues, but are unresolved
  // so they should be replaced with DeferredValueImpls if they exist in the context
  private final Set<String> usedDeferredWords;

  // These words are those which will be set to a value which has been deferred.
  private final Set<String> setDeferredWords;

  // Used to determine the combine scope
  private final CallStack macroStack;

  // Used to determine if in separate file
  private final String importResourcePath;

  public DeferredToken(Token token, Set<String> usedDeferredWords) {
    this.token = token;
    this.usedDeferredWords = getBases(usedDeferredWords);
    this.setDeferredWords = Collections.emptySet();
    importResourcePath = acquireImportResourcePath();
    macroStack = acquireMacroStack();
  }

  public DeferredToken(
    Token token,
    Set<String> usedDeferredWords,
    Set<String> setDeferredWords
  ) {
    this.token = token;
    this.usedDeferredWords = getBases(usedDeferredWords);
    this.setDeferredWords = getBases(setDeferredWords);
    importResourcePath = acquireImportResourcePath();
    macroStack = acquireMacroStack();
  }

  public Token getToken() {
    return token;
  }

  public Set<String> getUsedDeferredWords() {
    return usedDeferredWords;
  }

  public Set<String> getSetDeferredWords() {
    return setDeferredWords;
  }

  public String getImportResourcePath() {
    return importResourcePath;
  }

  public CallStack getMacroStack() {
    return macroStack;
  }

  public void addTo(Context context) {
    addTo(
      context,
      usedDeferredWords
        .stream()
        .filter(
          word -> {
            Object value = context.get(word);
            return value != null && !(value instanceof DeferredValue);
          }
        )
        .collect(Collectors.toCollection(HashSet::new))
    );
  }

  private void addTo(Context context, Set<String> wordsWithoutDeferredSource) {
    context.getDeferredTokens().add(this);
    deferPropertiesOnContext(context, wordsWithoutDeferredSource);
    if (context.getParent() != null) {
      Context parent = context.getParent();
      //Ignore global context
      if (parent.getParent() != null) {
        addTo(parent, wordsWithoutDeferredSource);
      } else {
        context.checkNumberOfDeferredTokens();
      }
    }
  }

  private void deferPropertiesOnContext(
    Context context,
    Set<String> wordsWithoutDeferredSource
  ) {
    wordsWithoutDeferredSource.forEach(word -> deferDuplicatePointers(context, word));
    wordsWithoutDeferredSource.removeAll(
      markDeferredWordsAndFindSources(context, wordsWithoutDeferredSource)
    );

    if (isInSameScope(context)) {
      // set props are only deferred when within the scope which the variable is set in
      markDeferredWordsAndFindSources(context, getSetDeferredWords());
    }
  }

  private boolean isInSameScope(Context context) {
    return (getMacroStack() == null || getMacroStack() == context.getMacroStack());
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

  private static String acquireImportResourcePath() {
    return (String) JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getContext().get(Context.IMPORT_RESOURCE_PATH_KEY))
      .filter(path -> path instanceof String)
      .orElse(null);
  }

  private static CallStack acquireMacroStack() {
    return JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getContext().getMacroStack())
      .orElse(null);
  }

  private static Set<String> getBases(Set<String> original) {
    return original
      .stream()
      .map(prop -> prop.split("\\.", 2)[0])
      .collect(Collectors.toSet());
  }
}
