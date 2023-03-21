package com.hubspot.jinjava.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.el.ELException;
import org.apache.commons.lang3.StringUtils;

@Beta
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
    Number.class
  );

  private static final Pattern NAMED_PARAMETER_KEY_PATTERN = Pattern.compile(
    "[\\w.]+=([^=]|$)"
  );
  private static final Pattern DICTIONARY_KEY_PATTERN = Pattern.compile("[\\w]+: ");

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
    try {
      if (val == null) {
        return JINJAVA_NULL;
      } else if (isResolvableObject(val)) {
        String pyishString = PyishObjectMapper.getAsPyishStringOrThrow(val);
        if (pyishString.length() < 1048576) { // TODO maybe this should be configurable
          return pyishString;
        }
      }
    } catch (IOException | OutputTooBigException ignored) {}
    throw new DeferredValueException("Can not convert deferred result to string");
  }

  // Find any unresolved variables, functions, etc in this expression to mark as deferred.
  private static Set<String> findDeferredWords(
    String partiallyResolved,
    JinjavaInterpreter interpreter
  ) {
    TokenScannerSymbols scannerSymbols = interpreter.getConfig().getTokenScannerSymbols();
    boolean nestedInterpretationEnabled = interpreter
      .getConfig()
      .isNestedInterpretationEnabled();
    boolean throwInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    FoundQuotedExpressionTags foundQuotedExpressionTags = new FoundQuotedExpressionTags();
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      Set<String> words = new HashSet<>();
      char[] value = partiallyResolved.toCharArray();
      int prevQuotePos = -1;
      int curPos = 0;
      char c;
      char prevChar = 0;
      boolean inQuote = false;
      char quoteChar = 0;
      while (curPos < partiallyResolved.length()) {
        c = value[curPos];
        if (inQuote) {
          if (c == quoteChar && prevChar != '\\') {
            if (nestedInterpretationEnabled) {
              getDeferredWordsInsideNestedExpression(
                interpreter,
                scannerSymbols,
                words,
                partiallyResolved.substring(prevQuotePos, curPos + 1),
                prevQuotePos,
                foundQuotedExpressionTags
              );
            }
            inQuote = false;
            prevQuotePos = curPos;
          }
        } else if ((c == '\'' || c == '"') && prevChar != '\\') {
          inQuote = true;
          quoteChar = c;
          words.addAll(
            findDeferredWordsInSubstring(
              partiallyResolved,
              prevQuotePos + 1,
              curPos,
              interpreter
            )
          );
          prevQuotePos = curPos;
        }
        prevChar = c;
        curPos++;
      }
      words.addAll(
        findDeferredWordsInSubstring(
          partiallyResolved,
          prevQuotePos + 1,
          curPos,
          interpreter
        )
      );

      if (foundQuotedExpressionTags.fullTagMayExist()) {
        throw new DeferredValueException(
          "Cannot get words inside nested interpretation tags"
        );
      }
      return words;
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(throwInterpreterErrorsStart);
    }
  }

  private static void getDeferredWordsInsideNestedExpression(
    JinjavaInterpreter interpreter,
    TokenScannerSymbols scannerSymbols,
    Set<String> words,
    String quoted,
    int offset,
    FoundQuotedExpressionTags foundQuotedExpressionTags
  ) {
    if (foundQuotedExpressionTags.firstStartTagFoundLocation == null) {
      int startWithIndex = quoted.indexOf(scannerSymbols.getExpressionStartWithTag());
      if (startWithIndex >= 0) {
        foundQuotedExpressionTags.firstStartTagFoundLocation = startWithIndex + offset;
      }
    }
    if (foundQuotedExpressionTags.firstStartTagFoundLocation != null) {
      int endWithIndex = quoted.indexOf(scannerSymbols.getExpressionEndWithTag());
      if (endWithIndex >= 0) {
        foundQuotedExpressionTags.lastEndTagFoundLocation = endWithIndex + offset;
      }
    }

    if (
      quoted.contains(scannerSymbols.getExpressionStart()) &&
      quoted.contains(scannerSymbols.getExpressionEnd())
    ) {
      List<ExpressionNode> expressionNodes = getExpressionNodes(
        WhitespaceUtils.unquoteAndUnescape(quoted),
        interpreter
      );
      words.addAll(
        expressionNodes
          .stream()
          .map(expressionNode -> ((ExpressionToken) expressionNode.getMaster()).getExpr())
          .map(expr -> findDeferredWords(expr, interpreter))
          .flatMap(Set::stream)
          .collect(Collectors.toSet())
      );
    }
  }

  private static List<ExpressionNode> getExpressionNodes(
    String input,
    JinjavaInterpreter interpreter
  ) {
    Node root = interpreter.parse(input);
    return getExpressionNodes(root).collect(Collectors.toList());
  }

  private static Stream<ExpressionNode> getExpressionNodes(Node parent) {
    if (parent instanceof ExpressionNode) {
      return Stream.of((ExpressionNode) parent);
    }
    return parent
      .getChildren()
      .stream()
      .flatMap(EagerExpressionResolver::getExpressionNodes);
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
        DICTIONARY_KEY_PATTERN.matcher(partiallyResolved).replaceAll(" ");
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
      return interpreter.resolveELExpressionSilently(w) == null;
    } catch (ELException | DeferredValueException | TemplateSyntaxException e) {
      return true;
    }
  }

  public static boolean isResolvableObject(Object val, int maxDepth, int maxSize) {
    return isResolvableObjectRec(val, 0, maxDepth, maxSize);
  }

  public static boolean isResolvableObject(Object val) {
    return isResolvableObjectRec(val, 0, 10, Integer.MAX_VALUE);
  }

  private static boolean isResolvableObjectRec(
    Object val,
    int depth,
    int maxDepth,
    int maxSize
  ) {
    if (depth > maxDepth) {
      return false;
    }
    if (isPrimitive(val)) {
      return true;
    }
    if (val instanceof Collection || val instanceof Map) {
      int size = val instanceof Collection
        ? ((Collection<?>) val).size()
        : ((Map<?, ?>) val).size();
      if (size == 0) {
        return true;
      } else if (size > maxSize) {
        return false;
      }
      return (
        val instanceof Collection ? (Collection<?>) val : ((Map<?, ?>) val).values()
      ).stream()
        .filter(Objects::nonNull)
        .allMatch(item -> isResolvableObjectRec(item, depth + 1, maxDepth, maxSize));
    } else if (val.getClass().isArray()) {
      if (((Object[]) val).length == 0) {
        return true;
      } else if (((Object[]) val).length > maxSize) {
        return false;
      }
      return (Arrays.stream((Object[]) val)).filter(Objects::nonNull)
        .allMatch(item -> isResolvableObjectRec(item, depth + 1, maxDepth, maxSize));
    }
    return PyishSerializable.class.isAssignableFrom(val.getClass());
  }

  public static boolean isPrimitive(Object val) {
    return (
      val == null || Primitives.isWrapperType(val.getClass()) || val instanceof String
    );
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

    public ResolutionState getResolutionState() {
      return resolutionState;
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

    /**
     * Method to supply a string value to the EagerExpressionResult class.
     * In the event that a DeferredValueException is thrown, the message will be the wrapped
     * value, and the resolutionState will be NONE
     * Manually provide whether the string has been fully resolved.
     * @param stringSupplier Supplier function to run, which could potentially throw a DeferredValueException.
     * @param interpreter The JinjavaInterpreter
     * @return A EagerExpressionResult that wraps either
     * <code>stringSupplier.get()</code> or the thrown DeferredValueException's message.
     */
    public static EagerExpressionResult fromSupplier(
      Supplier<String> stringSupplier,
      JinjavaInterpreter interpreter
    ) {
      try {
        return EagerExpressionResult.fromString(
          stringSupplier.get(),
          interpreter.getContext().getDeferredTokens().isEmpty()
            ? ResolutionState.FULL
            : ResolutionState.PARTIAL
        );
      } catch (DeferredValueException e) {
        return EagerExpressionResult.fromString(e.getMessage(), ResolutionState.NONE);
      }
    }

    public enum ResolutionState {
      FULL(true),
      PARTIAL(false),
      NONE(false);

      boolean fullyResolved;

      ResolutionState(boolean fullyResolved) {
        this.fullyResolved = fullyResolved;
      }
    }
  }

  private static class FoundQuotedExpressionTags {
    Integer firstStartTagFoundLocation;
    Integer lastEndTagFoundLocation;

    boolean fullTagMayExist() {
      return (
        firstStartTagFoundLocation != null &&
        lastEndTagFoundLocation != null &&
        firstStartTagFoundLocation < lastEndTagFoundLocation
      );
    }
  }
}
