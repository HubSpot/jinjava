package com.hubspot.jinjava.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.parse.Token;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
    "__macros__"
  );

  private static final Set<Class<?>> RESOLVABLE_CLASSES = ImmutableSet.of(
    String.class,
    Boolean.class,
    Number.class,
    PyishSerializable.class
  );

  // ( -> )
  // { -> }
  // [ -> ]
  private static final Map<Character, Character> CHUNK_LEVEL_MARKER_MAP = ImmutableMap.of(
    '(',
    ')',
    '{',
    '}',
    '[',
    ']'
  );

  private final String value;
  private final Token token;
  private final JinjavaInterpreter interpreter;
  private final Set<String> deferredWords;

  public ChunkResolver(String s, Token token, JinjavaInterpreter interpreter) {
    value = s;
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
  public String resolveChunks() {
    boolean isThrowInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    String bracketedResult;
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      bracketedResult =
        getValueAsJinjavaStringSafe(
          interpreter.resolveELExpression(
            String.format("[%s]", value),
            interpreter.getLineNumber()
          )
        );
    } catch (DeferredParsingException e) {
      deferredWords.addAll(findDeferredWords(e.getDeferredEvalResult()));
      bracketedResult = e.getDeferredEvalResult().trim();
    } catch (DeferredValueException e) {
      deferredWords.addAll(findDeferredWords(value));
      return value;
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(isThrowInterpreterErrorsStart);
    }
    // remove brackets
    String result = bracketedResult.substring(1, bracketedResult.length() - 1);
    if (JINJAVA_NULL.equals(result)) {
      // Resolved value of null as a string is ''.
      return JINJAVA_EMPTY_STRING;
    }
    return result;
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
    }
    return false;
  }

  private static String spaced(String toSpaceOut, String reference) {
    String prefix = reference.startsWith(" ") ? " " : "";
    String suffix = reference.endsWith(" ") ? " " : "";
    return prefix + toSpaceOut.trim() + suffix;
  }
}
