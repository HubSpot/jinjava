package com.hubspot.jinjava.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.objects.date.JsonPyishDateSerializer;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.tree.parse.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
  .registerModule(
      new SimpleModule().addSerializer(PyishDate.class, new JsonPyishDateSerializer())
    );

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
    "__macros__"
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
    boolean isHideInterpreterErrorsStart = interpreter
      .getContext()
      .getHideInterpreterErrors();
    try {
      interpreter.getContext().setHideInterpreterErrors(true);
      return String.join("", getChunk(null));
    } finally {
      interpreter.getContext().setHideInterpreterErrors(isHideInterpreterErrorsStart);
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
    boolean isHideInterpreterErrorsStart = interpreter
      .getContext()
      .getHideInterpreterErrors();
    try {
      interpreter.getContext().setHideInterpreterErrors(true);
      List<String> miniChunks = getChunk(null);
      return miniChunks
        .stream()
        .filter(s -> s.length() > 1 || !isMiniChunkSplitter(s.charAt(0)))
        .collect(Collectors.toList());
    } finally {
      interpreter.getContext().setHideInterpreterErrors(isHideInterpreterErrorsStart);
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
        prevChar = c;
        break;
      } else if (CHUNK_LEVEL_MARKER_MAP.containsKey(c)) {
        prevChar = c;
        tokenBuilder.append(c);
        tokenBuilder.append(resolveChunk(String.join("", getChunk(c))));
        tokenBuilder.append(prevChar);
        continue;
      } else if (isTokenSplitter(c)) {
        prevChar = c;

        miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
        tokenBuilder = new StringBuilder();
        if (isMiniChunkSplitter(c)) {
          chunks.add(resolveChunk(miniChunkBuilder.toString()));
          chunks.add(String.valueOf(c));
          miniChunkBuilder = new StringBuilder();
        } else {
          miniChunkBuilder.append(c);
        }
        continue;
      }
      prevChar = c;
      tokenBuilder.append(c);
    }
    miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
    chunks.add(resolveChunk(miniChunkBuilder.toString()));
    return chunks;
  }

  private boolean isTokenSplitter(char c) {
    return (!Character.isLetterOrDigit(c) && c != '_' && c != '.');
  }

  private boolean isMiniChunkSplitter(char c) {
    return c == ',';
  }

  private String resolveToken(String token) {
    if (StringUtils.isBlank(token)) {
      return "";
    }
    try {
      String resolvedToken;
      if (WhitespaceUtils.isQuoted(token) || RESERVED_KEYWORDS.contains(token)) {
        resolvedToken = token;
      } else {
        Object val = interpreter.retraceVariable(
          token,
          this.token.getLineNumber(),
          this.token.getStartPosition()
        );
        if (val == null) {
          try {
            val = interpreter.resolveELExpression(token, this.token.getLineNumber());
          } catch (UnknownTokenException e) {
            // val is still null
          }
        }
        if (val == null) {
          resolvedToken = token;
        } else {
          resolvedToken = getValueAsJinjavaString(val);
        }
      }
      return resolvedToken.trim();
    } catch (DeferredValueException | JsonProcessingException e) {
      deferredWords.addAll(findDeferredWords(token));
      return token.trim();
    }
  }

  // Try resolving the chunk/mini chunk as an ELExpression
  private String resolveChunk(String chunk) {
    if (StringUtils.isBlank(chunk)) {
      return "";
    } else if (RESERVED_KEYWORDS.contains(chunk)) {
      return chunk;
    }
    try {
      String resolvedChunk;
      Object val = interpreter.resolveELExpression(chunk, token.getLineNumber());
      if (val == null) {
        resolvedChunk = chunk;
      } else {
        resolvedChunk = getValueAsJinjavaString(val);
      }
      return resolvedChunk.trim();
    } catch (Exception e) {
      deferredWords.addAll(findDeferredWords(chunk));
      return chunk.trim();
    }
  }

  public static String getValueAsJinjavaString(Object val)
    throws JsonProcessingException {
    return OBJECT_MAPPER
      .writeValueAsString(val)
      .replaceAll("(?<!\\\\)(?:\\\\\\\\)*(')", "\\\\'")
      // Replace `\n` with a newline character
      .replaceAll("(?<!\\\\)(?:\\\\\\\\)*(\\\\n)", "\n")
      // Replace double-quotes with single quote as they are preferred in Jinja
      .replaceAll("(?<!\\\\)(?:\\\\\\\\)*(\")", "'")
      // Replace escaped double-quote with double quote character
      // Allows `"foo"` -> `"foo"` rather than `\"foo\"`
      .replaceAll("(?<!\\\\)(?:\\\\\\\\)*(\\\\\")", "\"");
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
    } catch (DeferredValueException e) {
      return true;
    }
  }
}
