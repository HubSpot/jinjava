package com.hubspot.jinjava.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.parse.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

  private final char[] value;
  private final int length;
  private final Token token;
  private final JinjavaInterpreter interpreter;
  private final Set<String> deferredWords;

  private int nextPos = 0;
  private char prevChar = 0;
  private boolean inQuote = false;
  private char quoteChar = 0;
  private boolean isAfterWhitespace = false;

  public ChunkResolver(String s, Token token, JinjavaInterpreter interpreter) {
    value = s.toCharArray();
    length = value.length;
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
    nextPos = 0;
    boolean isThrowInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      String expression = String.join("", getChunk(null)).trim();
      if (JINJAVA_NULL.equals(expression)) {
        // Resolved value of null as a string is ''.
        return JINJAVA_EMPTY_STRING;
      }
      return expression;
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(isThrowInterpreterErrorsStart);
    }
  }

  /**
   * Chunkify and resolve variables and expressions within the string.
   * Rather than concatenating the chunks, they are split by mini-chunks,
   * with the comma splitter ommitted from the list of results.
   * Therefore an expression of "1, 1 + 1, 1 + range(deferred)" becomes a List of ["1", "2", "1 + range(deferred)"].
   *
   * @return List of the expression chunk which is split into mini-chunks.
   */
  public List<String> splitChunks() {
    nextPos = 0;
    boolean isThrowInterpreterErrorsStart = interpreter
      .getContext()
      .getThrowInterpreterErrors();
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      List<String> miniChunks = getChunk(null);
      return miniChunks
        .stream()
        .filter(s -> s.length() > 1 || !isMiniChunkSplitter(s.charAt(0)))
        .map(String::trim)
        .collect(Collectors.toList());
    } finally {
      interpreter.getContext().setThrowInterpreterErrors(isThrowInterpreterErrorsStart);
    }
  }

  /**
   *  e.g. `[0, foo + bar]`:
   *     `0, foo + bar` is a chunk
   *     `0` and `foo + bar` are mini chunks
   *     `0`, `,`, ` `, `foo`, ` `, `+`, ` `, and `bar` are the tokens
   * @param chunkLevelMarker the marker `(`, `[`, `{` that started this chunk
   * @return the resolved chunk
   */
  private List<String> getChunk(Character chunkLevelMarker) {
    List<String> chunks = new ArrayList<>();
    // Mini chunks are split by commas.
    StringBuilder miniChunkBuilder = new StringBuilder();
    StringBuilder tokenBuilder = new StringBuilder();
    while (nextPos < length) {
      isAfterWhitespace = prevChar == ' ' && !isFilterWhitespace(prevChar);
      char c = value[nextPos++];
      if (inQuote) {
        if (c == quoteChar && prevChar != '\\') {
          inQuote = false;
        }
      } else if ((c == '\'' || c == '"') && prevChar != '\\') {
        inQuote = true;
        quoteChar = c;
      } else if (
        chunkLevelMarker != null && CHUNK_LEVEL_MARKER_MAP.get(chunkLevelMarker) == c
      ) {
        setPrevChar(c);
        break;
      } else if (CHUNK_LEVEL_MARKER_MAP.containsKey(c)) {
        setPrevChar(c);
        tokenBuilder.append(c);
        tokenBuilder.append(resolveChunk(String.join("", getChunk(c)), JINJAVA_NULL));
        tokenBuilder.append(prevChar);
        continue;
      } else if (isTokenSplitter(c)) {
        String resolvedToken = resolveToken(tokenBuilder.toString());
        if (StringUtils.isNotEmpty(resolvedToken)) {
          miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
        }
        tokenBuilder = new StringBuilder();
        if (isMiniChunkSplitter(c)) {
          chunks.add(resolveChunk(miniChunkBuilder.toString(), JINJAVA_NULL));
          chunks.add(String.valueOf(c));
          miniChunkBuilder = new StringBuilder();
        } else {
          miniChunkBuilder.append(c);
        }
        setPrevChar(c);
        continue;
      } else if (isAfterWhitespace) {
        // In case there is whitespace between words: `foo or bar`
        String resolvedToken = resolveToken(tokenBuilder.toString());
        if (StringUtils.isNotEmpty(resolvedToken)) {
          miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
        }
        tokenBuilder = new StringBuilder();
      }
      setPrevChar(c);
      tokenBuilder.append(c);
    }
    miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
    chunks.add(resolveChunk(miniChunkBuilder.toString(), JINJAVA_NULL));
    return chunks;
  }

  private void setPrevChar(char c) {
    if (c == '\\' && prevChar == '\\') {
      // Backslashes cancel each other out for escaping when there's an even number.
      prevChar = '\0';
    } else {
      prevChar = c;
    }
  }

  private boolean isTokenSplitter(char c) {
    return (
      !Character.isLetterOrDigit(c) && c != '_' && c != '.' && c != '|' && c != ' '
    );
  }

  private boolean isFilterWhitespace(char c) {
    // If a pipe character is surrounded by whitespace on either side,
    // we don't want to split those tokens
    boolean isFilterWhitespace = false;
    if (c == ' ') {
      int prevPos = nextPos - 2;
      if (nextPos < length) {
        isFilterWhitespace = value[nextPos] == ' ' || value[nextPos] == '|';
      }
      if (prevPos >= 0) {
        isFilterWhitespace =
          isFilterWhitespace || value[prevPos] == ' ' || value[prevPos] == '|';
      }
    }
    return isFilterWhitespace;
  }

  private boolean isMiniChunkSplitter(char c) {
    return c == ',';
  }

  private String resolveToken(String token) {
    if (StringUtils.isBlank(token)) {
      return token;
    }
    String resolvedToken = token;
    try {
      if (
        !WhitespaceUtils.isExpressionQuoted(token) && !RESERVED_KEYWORDS.contains(token)
      ) {
        Object val = null;
        try {
          val =
            interpreter.retraceVariable(
              token,
              this.token.getLineNumber(),
              this.token.getStartPosition()
            );
        } catch (TemplateSyntaxException ignored) {}
        if (val == null) {
          try {
            val = interpreter.resolveELExpression(token, this.token.getLineNumber());
          } catch (UnknownTokenException e) {
            // val is still null
          }
        }
        if (val == null || !isResolvableObject(val)) {
          resolvedToken = token;
        } else {
          resolvedToken =
            interpreter.getContext().getPyishObjectMapper().getAsPyishString(val);
        }
      }
    } catch (DeferredValueException e) {
      deferredWords.addAll(findDeferredWords(token));
    } catch (TemplateSyntaxException ignored) {}
    return spaced(resolvedToken, token);
  }

  // Try resolving the chunk/mini chunk as an ELExpression
  public String resolveChunk(String chunk, String nullDefault) {
    if (StringUtils.isBlank(chunk)) {
      return chunk;
    }
    String resolvedChunk = chunk;
    try {
      if (
        !WhitespaceUtils.isExpressionQuoted(chunk) && !RESERVED_KEYWORDS.contains(chunk)
      ) {
        try {
          Object val = interpreter.retraceVariable(
            chunk.trim(),
            this.token.getLineNumber(),
            this.token.getStartPosition()
          );
          if (val != null) {
            // If this isn't the final call, don't prematurely resolve complex objects.
            if (JINJAVA_NULL.equals(nullDefault) && !isResolvableObject(val)) {
              return chunk;
            }
          }
        } catch (TemplateSyntaxException ignored) {}

        Object val = interpreter.resolveELExpression(chunk, token.getLineNumber());
        if (val == null) {
          resolvedChunk = nullDefault;
        } else if (JINJAVA_NULL.equals(nullDefault) && !isResolvableObject(val)) {
          resolvedChunk = chunk;
        } else {
          resolvedChunk =
            interpreter.getContext().getPyishObjectMapper().getAsPyishString(val);
        }
      }
    } catch (TemplateSyntaxException ignored) {} catch (Exception e) {
      deferredWords.addAll(findDeferredWords(chunk));
    }
    return spaced(resolvedChunk, chunk);
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
      // Naively check if any element within val is resolvable,
      // rather than checking all of them, which may be costly.
      Optional<?> item =
        (
          val instanceof Collection ? (Collection<?>) val : ((Map<?, ?>) val).values()
        ).stream()
          .filter(Objects::nonNull)
          .findAny();
      if (item.isPresent()) {
        return RESOLVABLE_CLASSES
          .stream()
          .anyMatch(clazz -> clazz.isAssignableFrom(item.get().getClass()));
      }
    }
    return false;
  }

  private static String spaced(String toSpaceOut, String reference) {
    String prefix = reference.startsWith(" ") ? " " : "";
    String suffix = reference.endsWith(" ") ? " " : "";
    return prefix + toSpaceOut.trim() + suffix;
  }
}
