/*
 Copyright (c) 2014 HubSpot Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.hubspot.jinjava.tree.parse;

import static com.hubspot.jinjava.util.CharArrayUtils.charArrayRegionMatches;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.JinjavaConfig;
import java.util.HashMap;
import java.util.Map;

public class TokenScanner extends AbstractIterator<Token> {
  private final JinjavaConfig config;

  private static final Map<String, String> DEFAULT_RAW_TOKENS = ImmutableMap.of(
    "raw",
    "endraw"
  );

  private final char[] is;
  private final int length;

  private int currPost = 0;
  private int tokenStart = 0;
  private int tokenLength = 0;
  private int tokenKind = -1;
  private int lastStart = 0;
  private int inComment = 0;
  private int inRaw = 0;

  private String rawOpenTagName = null;
  private int inBlock = 0;
  private char inQuote = 0;
  private int currLine = 1;
  private int lastNewlinePos = 0;
  private TokenScannerSymbols symbols;

  public TokenScanner(String input, JinjavaConfig config) {
    this.config = config;

    is = input.toCharArray();
    length = is.length;

    currPost = 0;
    tokenStart = 0;
    tokenKind = -1;
    lastStart = 0;
    inComment = 0;
    inRaw = 0;
    rawOpenTagName = null;
    inBlock = 0;
    inQuote = 0;
    currLine = 1;
    lastNewlinePos = 0;

    symbols = config.getTokenScannerSymbols();
  }

  private Token getNextToken() {
    char c;
    while (currPost < length) {
      c = is[currPost++];
      if (currPost == length) {
        return getEndToken();
      }

      if (inBlock > 0) {
        if (c == '\\') {
          ++currPost;
          continue;
        } else if (inQuote != 0) {
          if (inQuote == c) {
            inQuote = 0;
          }
          continue;
        } else if (c == '\'' || c == '"') {
          inQuote = c;
          continue;
        }
      }

      // models switch case into if-else blocks
      if (c == symbols.getPrefix()) {
        if (currPost < length) {
          c = is[currPost];
          boolean startTokenFound = true;
          if (config.getLegacyOverrides().isWhitespaceRequiredWithinTokens()) {
            boolean hasNextChar = (currPost + 1) < length;
            boolean nextCharIsWhitespace = hasNextChar && (' ' == is[currPost + 1]);
            startTokenFound = nextCharIsWhitespace;
          }
          if (startTokenFound) {
            if (c == symbols.getNote()) {
              if (inComment == 1 || inRaw == 1) {
                continue;
              }
              inComment = 1;

              tokenLength = currPost - tokenStart - 1;
              if (tokenLength > 0) {
                // start a new token
                lastStart = tokenStart;
                tokenStart = --currPost;
                tokenKind = c;
                inComment = 0;
                return newToken(symbols.getFixed());
              } else {
                tokenKind = c;
              }
            } else if (c == symbols.getTag() || c == symbols.getExprStart()) {
              if (inComment > 0) {
                continue;
              }
              if (inRaw > 0 && (c == symbols.getExprStart() || !isEndRaw())) {
                continue;
              }
              // match token two ends
              if (!matchToken(c) && tokenKind > 0) {
                continue;
              }
              if (inBlock++ > 0) {
                continue;
              }

              tokenLength = currPost - tokenStart - 1;
              if (tokenLength > 0) {
                // start a new token
                lastStart = tokenStart;
                tokenStart = --currPost;
                tokenKind = c;
                return newToken(symbols.getFixed());
              } else {
                tokenKind = c;
              }
            }
          }
        } else { // reach the stream end
          return getEndToken();
        }
      } else if (c == symbols.getTag() || c == symbols.getExprEnd()) {
        // maybe current token is closing

        if (inComment > 0) {
          continue;
        }
        if (!matchToken(c)) {
          continue;
        }
        if (currPost < length) {
          c = is[currPost];
          if (c == symbols.getPostfix()) {
            inBlock = 0;

            tokenLength = currPost - tokenStart + 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = ++currPost;
              int kind = tokenKind;
              tokenKind = symbols.getFixed();
              return newToken(kind);
            }
          }
        } else {
          return getEndToken();
        }
      } else if (c == symbols.getNote()) { // case 3
        if (!matchToken(c)) {
          continue;
        }
        if (currPost < length) {
          c = is[currPost];
          if (c == symbols.getPostfix()) {
            inComment = 0;

            tokenLength = currPost - tokenStart + 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = ++currPost;
              tokenKind = symbols.getFixed();
              return newToken(symbols.getNote());
            }
          }
        } else {
          return getEndToken();
        }
      } else if (c == symbols.getNewline()) {
        currLine++;
        lastNewlinePos = currPost;

        if (inComment > 0 || inBlock > 0) {
          continue;
        }

        tokenKind = symbols.getFixed();
      } else {
        if (tokenKind == -1) {
          tokenKind = symbols.getFixed();
        }
      }
    }
    return null;
  }

  private boolean isEndRaw() {
    int pos = currPost + 1;
    while (pos < length) {
      if (!Character.isWhitespace(is[pos++])) {
        break;
      }
    }

    return charArrayRegionMatches(is, pos - 1, getRawTokens().get(rawOpenTagName));
  }

  private Token getEndToken() {
    tokenLength = currPost - tokenStart;
    int type = symbols.getFixed();
    if (inComment > 0) {
      type = symbols.getNote();
    } else if (inBlock > 0) {
      return new UnclosedToken(
        String.valueOf(is, tokenStart, tokenLength),
        currLine,
        tokenStart - lastNewlinePos + 1,
        symbols
      );
    }
    return Token.newToken(
      type,
      symbols,
      String.valueOf(is, tokenStart, tokenLength),
      currLine,
      tokenStart - lastNewlinePos + 1
    );
  }

  private Token newToken(int kind) {
    Token t = Token.newToken(
      kind,
      symbols,
      String.valueOf(is, lastStart, tokenLength),
      currLine,
      lastStart - lastNewlinePos + 1
    );

    if (t instanceof TagToken) {
      if (config.isTrimBlocks() && currPost < length && is[currPost] == '\n') {
        lastNewlinePos = currPost;
        ++currPost;
        ++tokenStart;
      }

      TagToken tt = (TagToken) t;
      if (getRawTokens().containsKey(tt.getTagName())) {
        inRaw = 1;
        rawOpenTagName = tt.getTagName();
        return tt;
      } else if (
        inRaw > 0 && getRawTokens().get(rawOpenTagName).equals(tt.getTagName())
      ) {
        inRaw = 0;
        rawOpenTagName = null;
        return tt;
      }
    }

    if (inRaw > 0 && t.getType() != symbols.getFixed()) {
      return Token.newToken(symbols.getFixed(), symbols, t.image, currLine, tokenStart);
    }

    return t;
  }

  private boolean matchToken(char kind) {
    if (kind == symbols.getExprStart()) {
      return tokenKind == symbols.getExprEnd();
    } else if (kind == symbols.getExprEnd()) {
      return tokenKind == symbols.getExprStart();
    } else {
      return kind == tokenKind;
    }
  }

  private Map<String, String> getRawTokens() {
    Map<String, String> rawTokens = new HashMap<>();
    rawTokens.putAll(DEFAULT_RAW_TOKENS);
    rawTokens.putAll(config.getRawTokens());
    return rawTokens;
  }

  @Override
  protected Token computeNext() {
    Token t = getNextToken();

    if (t == null) {
      return endOfData();
    }

    return t;
  }
}
