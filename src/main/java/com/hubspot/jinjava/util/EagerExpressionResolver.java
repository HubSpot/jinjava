package com.hubspot.jinjava.util;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.el.ELException;
import org.apache.commons.lang3.StringUtils;

public class EagerExpressionResolver {
  public static final String JINJAVA_NULL = "null";
  public static final String JINJAVA_EMPTY_STRING = "''";

  private static final Set<String> RESERVED_KEYWORDS = ImmutableSet.of(
    "and",
    "filter",
    "in",
    "is",
    "not",
    "or",
    "pluralize",
    "recursive",
    "trans",
    "null",
    "true",
    "false",
    "__macros__",
    ExtendedParser.INTERPRETER,
    "exptest",
    "filter"
  );

  private static final Set<Class<?>> RESOLVABLE_CLASSES = ImmutableSet.of(
    String.class,
    Boolean.class,
    Number.class,
    PyishSerializable.class
  );

  private static final Pattern NAMED_PARAMETER_KEY_PATTERN = Pattern.compile(
    "[\\w.]+=([^=]|$)"
  );
  private static final Pattern DICTIONARY_KEY_PATTERN = Pattern.compile("[\\w]: ");

  /**
   * Resolve the expression while handling deferred values.
   * Returns a EagerExpressionResult object which either holds the fully resolved object or a
   * partially resolved string as well as a set of any words that couldn't be resolved.
   * If a DeferredParsingException is thrown, the expression was partially resolved.
   * If a DeferredValueException is thrown, the expression could not be resolved at all.
   *
   * E.g with foo=3, bar=2:
   *   "range(0,foo)[-1] + deferred/bar" -> "2 + deferred/2"
   */
  public static EagerExpressionResult resolveExpression(
    String expression,
    JinjavaInterpreter interpreter
  ) {
    boolean fullyResolved = false;
    Set<String> deferredWords = new HashSet<>();
    Object result;
    try {
      result = interpreter.resolveELExpression(expression, interpreter.getLineNumber());
      fullyResolved = true;
    } catch (DeferredParsingException e) {
      deferredWords.addAll(findDeferredWords(e.getDeferredEvalResult(), interpreter));
      result = e.getDeferredEvalResult().trim();
    } catch (DeferredValueException e) {
      deferredWords.addAll(findDeferredWords(expression, interpreter));
      result = expression;
    } catch (TemplateSyntaxException e) {
      result = Collections.singletonList(null);
      fullyResolved = true;
    }
    return new EagerExpressionResult(
      result,
      deferredWords,
      fullyResolved ? ResolutionState.FULL : ResolutionState.PARTIAL
    );
  }

  public static String getValueAsJinjavaStringSafe(Object val) {
    if (val == null) {
      return JINJAVA_NULL;
    } else if (isResolvableObject(val)) {
      return PyishObjectMapper.getAsPyishString(val);
    }
    throw new DeferredValueException("Can not convert deferred result to string");
  }

  // Find any unresolved variables, functions, etc in this expression to mark as deferred.
  private static Set<String> findDeferredWords(
    String partiallyResolved,
    JinjavaInterpreter interpreter
  ) {
    boolean throwInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      Set<String> words = new HashSet<>();
      char[] value = partiallyResolved.toCharArray();
      int prevQuotePos = 0;
      int curPos = 0;
      char c;
      char prevChar = 0;
      boolean inQuote = false;
      char quoteChar = 0;
      while (curPos < partiallyResolved.length()) {
        c = value[curPos];
        if (inQuote) {
          if (c == quoteChar && prevChar != '\\') {
            inQuote = false;
            prevQuotePos = curPos;
          }
        } else if ((c == '\'' || c == '"') && prevChar != '\\') {
          inQuote = true;
          quoteChar = c;
          words.addAll(
            findDeferredWordsInSubstring(
              partiallyResolved,
              prevQuotePos,
              curPos,
              interpreter
            )
          );
        }
        prevChar = c;
        curPos++;
      }
      words.addAll(
        findDeferredWordsInSubstring(partiallyResolved, prevQuotePos, curPos, interpreter)
      );
      return words;
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(throwInterpreterErrorsStart);
    }
  }

  // Knowing that there are no quotes between start and end,
  // split up the words in `partiallyResolved` and return whichever ones can't be resolved.
  private static Set<String> findDeferredWordsInSubstring(
    String partiallyResolved,
    int start,
    int end,
    JinjavaInterpreter interpreter
  ) {
    partiallyResolved = partiallyResolved.substring(start, end);
    if (!interpreter.getConfig().getLegacyOverrides().isEvaluateMapKeys()) {
      partiallyResolved =
        DICTIONARY_KEY_PATTERN.matcher(partiallyResolved).replaceAll("");
    }
    return Arrays
      .stream(
        NAMED_PARAMETER_KEY_PATTERN
          .matcher(partiallyResolved)
          .replaceAll("$1")
          .split("[^\\w.]")
      )
      .filter(StringUtils::isNotBlank)
      .filter(w -> shouldBeEvaluated(w, interpreter))
      .collect(Collectors.toSet());
  }

  public static boolean shouldBeEvaluated(String w, JinjavaInterpreter interpreter) {
    try {
      if (RESERVED_KEYWORDS.contains(w)) {
        return false;
      }
      try {
        Object val = interpreter.retraceVariable(w, interpreter.getLineNumber());
        if (val != null) {
          // It's a variable that must now be deferred
          return true;
        }
      } catch (UnknownTokenException e) {
        // val is still null
      }
      // don't defer numbers, values such as true/false, etc.
      return interpreter.resolveELExpression(w, interpreter.getLineNumber()) == null;
    } catch (ELException | DeferredValueException | TemplateSyntaxException e) {
      return true;
    }
  }

  public static boolean isResolvableObject(Object val) {
    return isResolvableObjectRec(val, 0);
  }

  private static boolean isResolvableObjectRec(Object val, int depth) {
    if (depth > 10) {
      return false;
    }
    boolean isResolvable = RESOLVABLE_CLASSES
      .stream()
      .anyMatch(clazz -> clazz.isAssignableFrom(val.getClass()));
    if (isResolvable) {
      return true;
    }
    if (val instanceof Collection || val instanceof Map) {
      if (
        val instanceof Collection
          ? ((Collection<?>) val).isEmpty()
          : ((Map<?, ?>) val).isEmpty()
      ) {
        return true;
      }
      return (
        val instanceof Collection ? (Collection<?>) val : ((Map<?, ?>) val).values()
      ).stream()
        .filter(Objects::nonNull)
        .allMatch(item -> isResolvableObjectRec(item, depth + 1));
    } else if (val.getClass().isArray()) {
      if (((Object[]) val).length == 0) {
        return true;
      }
      return (Arrays.stream((Object[]) val)).filter(Objects::nonNull)
        .allMatch(item -> isResolvableObjectRec(item, depth + 1));
    }
    return false;
  }

  public static class EagerExpressionResult {
    private final Object resolvedObject;
    private final Set<String> deferredWords;
    private final ResolutionState resolutionState;

    private EagerExpressionResult(
      Object resolvedObject,
      Set<String> deferredWords,
      ResolutionState resolutionState
    ) {
      this.resolvedObject = resolvedObject;
      this.deferredWords = deferredWords;
      this.resolutionState = resolutionState;
    }

    /**
     * Returns a string representation of the resolved expression.
     * If there are multiple, they will be separated by commas,
     * but not surrounded with brackets.
     * @return String representation of the result.
     */
    @Override
    public String toString() {
      return toString(false);
    }

    /**
     * When forOutput is true, the result will always be unquoted.
     * @param forOutput Whether the result is going to be included in the final output,
     *                  such as in an expression, or not such as when reconstructing tags.
     * @return String representation of the result
     */
    public String toString(boolean forOutput) {
      if (!resolutionState.fullyResolved) {
        return (String) resolvedObject;
      }
      if (resolvedObject == null) {
        return forOutput ? "" : JINJAVA_EMPTY_STRING;
      }
      String asString;
      JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
      if (forOutput && interpreter != null) {
        asString = interpreter.getAsString(resolvedObject);
      } else {
        asString = PyishObjectMapper.getAsPyishString(resolvedObject);
      }
      return asString;
    }

    public List<?> toList() {
      if (resolutionState.fullyResolved) {
        if (resolvedObject instanceof List) {
          return (List<?>) resolvedObject;
        } else {
          return Collections.singletonList(resolvedObject);
        }
      }
      throw new DeferredValueException("Object is not resolved");
    }

    public boolean isFullyResolved() {
      return resolutionState.fullyResolved;
    }

    public Set<String> getDeferredWords() {
      return deferredWords;
    }

    /**
     * Method to wrap a string value in the EagerExpressionResult class.
     * It is not evaluated, rather it's allows a the class to be manually
     * built from a partially resolved string.
     * @param resolvedString Partially resolved string to wrap.
     * @return A EagerExpressionResult that {@link #toString()} returns <code>resolvedString</code>.
     */
    public static EagerExpressionResult fromString(String resolvedString) {
      return new EagerExpressionResult(
        resolvedString,
        Collections.emptySet(),
        ResolutionState.PARTIAL
      );
    }

    /**
     * Method to wrap a string value in the EagerExpressionResult class.
     * Manually provide whether the string has been fully resolved.
     * @param resolvedString Partially or fully resolved string to wrap
     * @param resolutionState Either FULL or PARTIAL
     * @return A EagerExpressionResult that {@link #toString()} returns <code>resolvedString</code>.
     */
    public static EagerExpressionResult fromString(
      String resolvedString,
      ResolutionState resolutionState
    ) {
      return new EagerExpressionResult(
        resolvedString,
        Collections.emptySet(),
        resolutionState
      );
    }

    public enum ResolutionState {
      FULL(true),
      PARTIAL(false);

      boolean fullyResolved;

      ResolutionState(boolean fullyResolved) {
        this.fullyResolved = fullyResolved;
      }
    }
  }
}
