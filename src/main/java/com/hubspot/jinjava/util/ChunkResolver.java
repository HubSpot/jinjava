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
import com.hubspot.jinjava.tree.parse.Token;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * This class takes a string and resolves it in chunks. This allows for
 * strings with deferred values within them to be partially resolved, as much
 * as they can be with a deferred value.
 * E.g with foo=3, bar=2:
 *   "range(0,foo)[-1] + deferred/bar" -> "2 + deferred/2"
 * This class is not thread-safe. Do not reuse between threads.
 */
public class ChunkResolver {
  private static final String JINJAVA_NULL = "null";
  private static final String JINJAVA_EMPTY_STRING = "''";

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

  private final String value;
  private final Token token;
  private final JinjavaInterpreter interpreter;
  private final Set<String> deferredWords;

  public ChunkResolver(String s, Token token, JinjavaInterpreter interpreter) {
    value = s.trim();
    this.token = token;
    this.interpreter = interpreter;
    deferredWords = new HashSet<>();
  }

  /**
   * @return Any deferred words that were encountered.
   */
  public Set<String> getDeferredWords() {
    return deferredWords;
  }

  /**
   * Chunkify and resolve variables and expressions within the string.
   * Tokens are resolved within "chunks" where a chunk is surrounded by a markers
   * of {}, [], (). The contents inside of a chunk are split by whitespace
   * and/or comma, and these "tokens" resolved individually.
   *
   * The main chunk itself does not get resolved.
   * e.g.
   *  `false || (foo), 'bar'` -> `true, 'bar'`
   *  `[(foo == bar), deferred, bar]` -> `[true,deferred,'hello']`
   * @return String with chunk layers within it being partially or fully resolved.
   */
  public ResolvedChunks resolveChunks() {
    boolean fullyResolved = false;
    Object result;
    try {
      result = interpreter.resolveELExpression(value, interpreter.getLineNumber());
      fullyResolved = true;
    } catch (DeferredParsingException e) {
      deferredWords.addAll(findDeferredWords(e.getDeferredEvalResult()));
      result = e.getDeferredEvalResult().trim();
    } catch (DeferredValueException e) {
      deferredWords.addAll(findDeferredWords(value));
      result = value;
    } catch (TemplateSyntaxException e) {
      result = Collections.singletonList(null);
      fullyResolved = true;
    }
    return new ResolvedChunks(result, fullyResolved);
  }

  public static String getValueAsJinjavaStringSafe(Object val) {
    if (val == null) {
      return JINJAVA_NULL;
    } else if (isResolvableObject(val)) {
      return PyishObjectMapper.getAsPyishString(val);
    }
    throw new DeferredValueException("Can not convert deferred result to string");
  }

  // Find any variables, functions, etc in this chunk to mark as deferred.
  // similar processing to getChunk method, but without recursion.
  private Set<String> findDeferredWords(String chunk) {
    boolean throwInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      Set<String> words = new HashSet<>();
      char[] value = chunk.toCharArray();
      int prevQuotePos = 0;
      int curPos = 0;
      char c;
      char prevChar = 0;
      boolean inQuote = false;
      char quoteChar = 0;
      while (curPos < chunk.length()) {
        c = value[curPos];
        if (inQuote) {
          if (c == quoteChar && prevChar != '\\') {
            inQuote = false;
            prevQuotePos = curPos;
          }
        } else if ((c == '\'' || c == '"') && prevChar != '\\') {
          inQuote = true;
          quoteChar = c;
          words.addAll(findDeferredWordsInSubstring(chunk, prevQuotePos, curPos));
        }
        prevChar = c;
        curPos++;
      }
      words.addAll(findDeferredWordsInSubstring(chunk, prevQuotePos, curPos));
      return words;
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(throwInterpreterErrorsStart);
    }
  }

  // Knowing that there are no quotes between start and end,
  // split up the words in `chunk` and return whichever ones can't be resolved.
  private Set<String> findDeferredWordsInSubstring(String chunk, int start, int end) {
    return Arrays
      .stream(chunk.substring(start, end).split("[^\\w.]"))
      .filter(StringUtils::isNotBlank)
      .filter(w -> shouldBeEvaluated(w, token, interpreter))
      .collect(Collectors.toSet());
  }

  public static boolean shouldBeEvaluated(
    String w,
    Token token,
    JinjavaInterpreter interpreter
  ) {
    try {
      if (RESERVED_KEYWORDS.contains(w)) {
        return false;
      }
      try {
        Object val = interpreter.retraceVariable(
          w,
          token.getLineNumber(),
          token.getStartPosition()
        );
        if (val != null) {
          // It's a variable that must now be deferred
          return true;
        }
      } catch (UnknownTokenException e) {
        // val is still null
      }
      // don't defer numbers, values such as true/false, etc.
      return interpreter.resolveELExpression(w, token.getLineNumber()) == null;
    } catch (DeferredValueException | TemplateSyntaxException e) {
      return true;
    }
  }

  private static boolean isResolvableObject(Object val) {
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

  public static class ResolvedChunks {
    private final Object resolvedObject;
    private final boolean fullyResolved;

    private ResolvedChunks(Object resolvedObject, boolean fullyResolved) {
      this.resolvedObject = resolvedObject;
      this.fullyResolved = fullyResolved;
    }

    /**
     * Returns a string representation of the resolved expression.
     * If there are multiple, they will be separated by commas,
     * but not surrounded with brackets.
     * @return String representation of the chunks.
     */
    @Override
    public String toString() {
      return toString(false);
    }

    /**
     * When forOutput is true, the result will always be unquoted.
     * @param forOutput Whether the result is going to be included in the final output,
     *                  such as in an expression, or not such as when reconstructing tags.
     * @return String representation of the chunks
     */
    public String toString(boolean forOutput) {
      if (!fullyResolved) {
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
      if (fullyResolved) {
        if (resolvedObject instanceof List) {
          return (List<?>) resolvedObject;
        } else {
          return Collections.singletonList(resolvedObject);
        }
      }
      throw new DeferredValueException("Object is not resolved");
    }

    public boolean isFullyResolved() {
      return fullyResolved;
    }

    /**
     * Method to wrap a string value in the ResolvedChunks class.
     * It is not evaluated, rather it's allows a the class to be manually
     * built from a partially resolved string.
     * @param resolvedString Partially resolved string to wrap.
     * @return A ResolvedChunks that {@link #toString()} returns <code>resolvedString</code>.
     */
    public static ResolvedChunks fromString(String resolvedString) {
      return new ResolvedChunks(resolvedString, false);
    }
  }
}
