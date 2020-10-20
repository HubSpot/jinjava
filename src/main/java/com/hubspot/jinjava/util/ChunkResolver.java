package com.hubspot.jinjava.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is not thread-safe. Do not reuse between threads.
 */
public class ChunkResolver {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
  private final TagToken tagToken;
  private final JinjavaInterpreter interpreter;
  private final Set<String> deferredVariables;

  private boolean useMiniChunks = true;
  private int nextPos = 0;
  private char prevChar = 0;
  private boolean inQuote = false;
  private char quoteChar = 0;

  public ChunkResolver(String s, TagToken tagToken, JinjavaInterpreter interpreter) {
    value = s.toCharArray();
    length = value.length;
    this.tagToken = tagToken;
    this.interpreter = interpreter;
    deferredVariables = new HashSet<>();
  }

  /**
   * use Comma as token/mini chunk split or not true use it; false don't use it.
   *
   * @param onOrOff
   *          flag to indicate whether or not to split on commas
   * @return this instance for method chaining
   */
  public ChunkResolver useMiniChunks(boolean onOrOff) {
    useMiniChunks = onOrOff;
    return this;
  }

  /**
   * @return Any deferred variables that were encountered.
   */
  public Set<String> getDeferredVariables() {
    return deferredVariables;
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
    return getChunk(null);
  }

  /**
   *  e.g. `[0, foo + bar]`:
   *     `0, foo + bar` is a chunk
   *     `0` and `foo + bar` are mini chunks
   *     `0`, `,`, ` `, `foo`, ` `, `+`, ` `, and `bar` are the tokens
   * @param chunkLevelMarker the marker `(`, `[`, `{` that started this chunk
   * @return the resolved chunk
   */
  private String getChunk(Character chunkLevelMarker) {
    StringBuilder chunkBuilder = new StringBuilder();
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
        tokenBuilder.append(resolveChunk(getChunk(c)));
        tokenBuilder.append(prevChar);
        continue;
      } else if (isTokenSplitter(c)) {
        prevChar = c;

        miniChunkBuilder.append(resolveToken(tokenBuilder.toString()));
        tokenBuilder = new StringBuilder();
        if (isMiniChunkSplitter(c)) {
          chunkBuilder.append(resolveChunk(miniChunkBuilder.toString()));
          chunkBuilder.append(c);
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
    chunkBuilder.append(resolveChunk(miniChunkBuilder.toString()));
    return chunkBuilder.toString();
  }

  private boolean isTokenSplitter(char c) {
    //    return Character.isWhitespace(c) || (useMiniChunks && c == ',');
    // regex \w
    return !(Character.isLetterOrDigit(c) || c == '_' || c == '.');
  }

  private boolean isMiniChunkSplitter(char c) {
    return useMiniChunks && c == ',';
  }

  private String resolveToken(String token) {
    if (StringUtils.isBlank(token)) {
      return "";
    }
    try {
      String resolvedToken;
      if (WhitespaceUtils.isQuoted(token)) {
        resolvedToken = token;
      } else {
        Object val = interpreter.retraceVariable(
          token,
          tagToken.getLineNumber(),
          tagToken.getStartPosition()
        );
        if (val == null) {
          try {
            val = interpreter.resolveELExpression(token, tagToken.getLineNumber());
          } catch (UnknownTokenException e) {
            // val is still null
          }
        }
        if (val == null) {
          resolvedToken = token;
        } else {
          if (val instanceof String) {
            resolvedToken = String.format("'%s'", val);
          } else {
            return OBJECT_MAPPER.writeValueAsString(val);
          }
        }
      }
      return resolvedToken.trim();
    } catch (DeferredValueException | JsonProcessingException e) {
      deferredVariables.addAll(findDeferredVariables(token));
      return token.trim();
    }
  }

  // Try resolving the chunk/mini chunk as an ELExpression
  private String resolveChunk(String chunk) {
    if (StringUtils.isBlank(chunk)) {
      return "";
    }
    try {
      String resolvedChunk;
      Object val = interpreter.resolveELExpression(chunk, tagToken.getLineNumber());
      if (val == null) {
        resolvedChunk = chunk;
      } else {
        if (val instanceof String) {
          resolvedChunk = String.format("'%s'", val);
        } else {
          return OBJECT_MAPPER.writeValueAsString(val);
        }
      }
      return resolvedChunk.trim();
    } catch (Exception e) {
      findDeferredVariables(chunk);
      return chunk.trim();
    }
  }

  // Find any variables, functions, etc in this chunk and mark as deferred.
  // similar processing to getChunk method, but without recursion.
  private Set<String> findDeferredVariables(String chunk) {
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
        words.addAll(findWords(chunk, prevQuotePos, curPos));
      }
      prevChar = c;
      curPos++;
    }
    words.addAll(findWords(chunk, prevQuotePos, curPos));
    return words;
  }

  // Knowing that there are no quotes between start and end,
  // split up the words in `chunk` and return whichever ones can't be resolved.
  private Set<String> findWords(String chunk, int start, int end) {
    return Arrays
      .stream(chunk.substring(start, end).split("[^\\w]"))
      .filter(StringUtils::isNotBlank)
      .filter(
        w -> {
          try {
            // don't defer numbers, values such as true/false, etc.
            return interpreter.resolveELExpression(w, tagToken.getLineNumber()) == null;
          } catch (DeferredValueException e) {
            return true;
          }
        }
      )
      .collect(Collectors.toSet());
  }
}
