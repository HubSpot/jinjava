package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.hubspot.jinjava.interpret.CallStack;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredLazyReference;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Beta
public class DeferredToken {

  public static class DeferredTokenBuilder {

    private final Token token;
    private Stream<String> usedDeferredWords;
    private Stream<String> setDeferredWords;

    private DeferredTokenBuilder(Token token) {
      this.token = token;
    }

    public DeferredToken build() {
      JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
      return new DeferredToken(
        token,
        usedDeferredWords != null
          ? usedDeferredWords
            .map(DeferredToken::splitToken)
            .map(DeferredToken::getFirstNonEmptyToken)
            .distinct()
            .filter(word ->
              interpreter == null ||
              !(interpreter.getContext().get(word) instanceof DeferredMacroValueImpl)
            )
            .collect(Collectors.toSet())
          : Collections.emptySet(),
        setDeferredWords != null
          ? setDeferredWords
            .map(DeferredToken::splitToken)
            .map(DeferredToken::getFirstNonEmptyToken)
            .collect(Collectors.toSet())
          : Collections.emptySet(),
        acquireImportResourcePath(),
        acquireMacroStack()
      );
    }

    public DeferredTokenBuilder addUsedDeferredWords(
      Collection<String> usedDeferredWordsToAdd
    ) {
      return addUsedDeferredWords(usedDeferredWordsToAdd.stream());
    }

    public DeferredTokenBuilder addUsedDeferredWords(
      Stream<String> usedDeferredWordsToAdd
    ) {
      if (usedDeferredWords == null) {
        usedDeferredWords = usedDeferredWordsToAdd;
      } else {
        usedDeferredWords = Stream.concat(usedDeferredWords, usedDeferredWordsToAdd);
      }
      return this;
    }

    public DeferredTokenBuilder addSetDeferredWords(
      Collection<String> setDeferredWordsToAdd
    ) {
      return addSetDeferredWords(setDeferredWordsToAdd.stream());
    }

    public DeferredTokenBuilder addSetDeferredWords(
      Stream<String> setDeferredWordsToAdd
    ) {
      if (setDeferredWords == null) {
        setDeferredWords = setDeferredWordsToAdd;
      } else {
        setDeferredWords = Stream.concat(setDeferredWords, setDeferredWordsToAdd);
      }
      return this;
    }
  }

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

  /**
   * Create a {@link DeferredTokenBuilder} with the provided {@link Token} {@code token}
   * @param token A {@link Token} with a deferred image
   * @return DeferredTokenBuilder
   */
  public static DeferredTokenBuilder builderFromToken(Token token) {
    return new DeferredTokenBuilder(token);
  }

  /**
   * Create a {@link DeferredTokenBuilder} with a {@link Token} constructed using the constructor of {@code tokenClass} using
   * the provided {@code image} and line number, position, and symbols taken from the {@code interpreter}.
   * @param image The deferred token image
   * @param tokenClass Class of {@link Token} to create
   * @param interpreter The {@link JinjavaInterpreter}
   * @return DeferredTokenBuilder
   * @param <T> generic type of the {@tokenClass}, which extends {@link Token}
   */
  public static <T extends Token> DeferredTokenBuilder builderFromImage(
    String image,
    Class<T> tokenClass,
    JinjavaInterpreter interpreter
  ) {
    return builderFromToken(
      constructToken(
        tokenClass,
        image,
        interpreter.getLineNumber(),
        interpreter.getPosition(),
        interpreter.getConfig().getTokenScannerSymbols()
      )
    );
  }

  /**
   * Create a {@link DeferredTokenBuilder} with a {@link Token} constructed using the provided {@code image}
   * and line number, position, and symbols taken from the {@code originalToken}.
   * @param image The deferred token image
   * @param originalToken Original {@link Token} to reference for attributes
   * @return DeferredTokenBuilder
   */
  public static DeferredTokenBuilder builderFromImage(String image, Token originalToken) {
    return builderFromToken(
      constructToken(
        originalToken.getClass(),
        image,
        originalToken.getLineNumber(),
        originalToken.getStartPosition(),
        originalToken.getSymbols()
      )
    );
  }

  private static <T extends Token> T constructToken(
    Class<T> tokenClass,
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols
  ) {
    try {
      return tokenClass
        .getDeclaredConstructor(
          String.class,
          int.class,
          int.class,
          TokenScannerSymbols.class
        )
        .newInstance(image, lineNumber, startPosition, symbols);
    } catch (
      InstantiationException
      | IllegalAccessException
      | InvocationTargetException
      | NoSuchMethodException e
    ) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @deprecated Use {@link #builderFromToken(Token)}
   */
  @Deprecated
  public DeferredToken(Token token, Set<String> usedDeferredWords) {
    this(token, usedDeferredWords, Collections.emptySet());
  }

  /**
   * @deprecated Use {@link #builderFromToken(Token)}
   */
  @Deprecated
  public DeferredToken(
    Token token,
    Set<String> usedDeferredWords,
    Set<String> setDeferredWords
  ) {
    this(
      token,
      getBases(usedDeferredWords),
      getBases(setDeferredWords),
      acquireImportResourcePath(),
      acquireMacroStack()
    );
  }

  private DeferredToken(
    Token token,
    Set<String> usedDeferredWordBases,
    Set<String> setDeferredWordBases,
    String importResourcePath,
    CallStack macroStack
  ) {
    this.token = token;
    this.usedDeferredWords = usedDeferredWordBases;
    this.setDeferredWords = setDeferredWordBases;
    this.importResourcePath = importResourcePath;
    this.macroStack = macroStack;
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
        .filter(word -> {
          Object value = context.get(word);
          return value != null && !(value instanceof DeferredValue);
        })
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
    if (isInSameScope(context)) {
      // set props are only deferred when within the scope which the variable is set in
      markDeferredWordsAndFindSources(context, getSetDeferredWords(), true);
    }
    wordsWithoutDeferredSource.forEach(word -> deferDuplicatePointers(context, word));
    wordsWithoutDeferredSource.removeAll(
      markDeferredWordsAndFindSources(context, wordsWithoutDeferredSource, false)
    );
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
          .filter(entry ->
            entry.getValue() == wordValue ||
            (
              entry.getValue() instanceof DeferredValue &&
              ((DeferredValue) entry.getValue()).getOriginalValue() == wordValue
            )
          )
          .forEach(entry -> {
            matchingEntries.add(entry);
            deferredLazyReference.getOriginalValue().setReferenceKey(entry.getKey());
          });
        temp = temp.getParent();
      }
      if (matchingEntries.size() > 1) { // at least one duplicate
        matchingEntries.forEach(entry -> {
          if (
            deferredLazyReference
              .getOriginalValue()
              .getReferenceKey()
              .equals(entry.getKey())
          ) {
            convertToDeferredLazyReferenceSource(context, entry);
          } else {
            entry.setValue(deferredLazyReference.clone());
          }
        });
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
    DeferredLazyReferenceSource deferredLazyReferenceSource =
      DeferredLazyReferenceSource.instance(
        val instanceof DeferredValue ? ((DeferredValue) val).getOriginalValue() : val
      );

    context.replace(entry.getKey(), deferredLazyReferenceSource);
    entry.setValue(deferredLazyReferenceSource);
  }

  private static Collection<String> markDeferredWordsAndFindSources(
    Context context,
    Set<String> wordsToDefer,
    boolean replacing
  ) {
    return wordsToDefer
      .stream()
      .filter(prop -> {
        Object val = context.get(prop);
        if (replacing) {
          return (
            !(val instanceof DeferredValue) || context.getScope().containsKey(prop)
          );
        }
        return !(val instanceof DeferredValue);
      })
      .filter(prop -> !context.getMetaContextVariables().contains(prop))
      .filter(prop -> {
        DeferredValue deferredValue = convertToDeferredValue(context, prop);
        context.put(prop, deferredValue);
        return !(deferredValue instanceof DeferredValueShadow);
      })
      .collect(Collectors.toList());
  }

  private static DeferredValue convertToDeferredValue(Context context, String prop) {
    Object valueInScope = context.getScope().get(prop);
    Object value = context.get(prop);
    if (value instanceof DeferredValue) {
      value = ((DeferredValue) value).getOriginalValue();
    }
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

  private static String getFirstNonEmptyToken(List<String> strings) {
    return Strings.isNullOrEmpty(strings.get(0)) ? strings.get(1) : strings.get(0);
  }

  public static List<String> splitToken(String token) {
    return Arrays.asList(token.split("\\.", 2));
  }

  public static Set<String> getBases(Set<String> original) {
    return original
      .stream()
      .map(DeferredToken::splitToken)
      .map(prop -> prop.get(0))
      .collect(Collectors.toSet());
  }
}
