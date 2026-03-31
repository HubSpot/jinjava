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
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.BuiltInFeatures;

public class TokenScanner extends AbstractIterator<Token> {

  private final JinjavaConfig config;

  private final char[] is;
  private final int length;

  private int currPost = 0;
  private int tokenStart = 0;
  private int tokenLength = 0;
  private int tokenKind = -1;
  private int lastStart = 0;
  private int inComment = 0;
  private int inRaw = 0;
  private int inBlock = 0;
  private char inQuote = 0;
  private int currLine = 1;
  private int lastNewlinePos = 0;
  private final TokenScannerSymbols symbols;
  private final WhitespaceControlParser whitespaceControlParser;

  // String-based path state — only populated when symbols.isStringBased() == true.
  private final boolean stringBased;
  private final char[] varStart;
  private final char[] varEnd;
  private final char[] blkStart;
  private final char[] blkEnd;
  private final char[] cmtStart;
  private final char[] cmtEnd;

  // Remembers the position where the current opening delimiter began so that the
  // emitted block/comment token image starts from the opener, not the content.
  private int blockOpenerStart = 0;

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
    inBlock = 0;
    inQuote = 0;
    currLine = 1;
    lastNewlinePos = 0;
    blockOpenerStart = 0;

    symbols = config.getTokenScannerSymbols();
    stringBased = symbols.isStringBased();
    whitespaceControlParser =
      config.getLegacyOverrides().isParseWhitespaceControlStrictly()
        ? WhitespaceControlParser.STRICT
        : WhitespaceControlParser.LENIENT;

    if (stringBased) {
      varStart = symbols.getExpressionStart().toCharArray();
      varEnd = symbols.getExpressionEnd().toCharArray();
      blkStart = symbols.getExpressionStartWithTag().toCharArray();
      blkEnd = symbols.getExpressionEndWithTag().toCharArray();
      cmtStart = symbols.getOpeningComment().toCharArray();
      cmtEnd = symbols.getClosingComment().toCharArray();
    } else {
      varStart = varEnd = blkStart = blkEnd = cmtStart = cmtEnd = null;
    }
  }

  // ── Dispatch ───────────────────────────────────────────────────────────────

  private Token getNextToken() {
    return stringBased ? getNextTokenStringBased() : getNextTokenCharBased();
  }

  // ── String-based scanning path ─────────────────────────────────────────────
  //
  // Design:
  //
  //   tokenStart      — start of the next text region to buffer (updated after
  //                     each emitted token to point just past the emitted content).
  //   blockOpenerStart — position of the opening delimiter character; the emitted
  //                     block/comment token image begins here so that the token's
  //                     parse() method can strip the correct number of delimiter
  //                     characters from both ends.
  //   lastStart/tokenLength — the slice passed to Token.newToken().
  //
  // Two-phase emission:
  //   1. Opener detected → flush any buffered plain text as a TEXT token (using
  //      is[tokenStart..openerPos)). Record blockOpenerStart = openerPos. Advance
  //      tokenStart and currPost past the opener into the block content.
  //   2. Closer detected → emit the full delimited image (is[blockOpenerStart..
  //      closerEnd)) as the appropriate token type. Advance tokenStart = currPost
  //      = closerEnd so the next iteration starts after the closer.

  private Token getNextTokenStringBased() {
    while (currPost < length) {
      char c = is[currPost];

      // Track newlines for accurate line/column numbers.
      if (c == '\n') {
        currLine++;
        lastNewlinePos = currPost + 1;
      }

      // ── State: inside a comment ────────────────────────────────────────────
      if (inComment > 0) {
        if (regionMatches(currPost, cmtEnd)) {
          // Emit from the opener start to the end of the closer.
          lastStart = blockOpenerStart;
          tokenLength = currPost + cmtEnd.length - blockOpenerStart;
          tokenStart = currPost + cmtEnd.length;
          currPost = tokenStart;
          inComment = 0;
          int kind = tokenKind;
          tokenKind = symbols.getFixed();
          return emitStringToken(kind);
        }
        currPost++;
        continue;
      }

      // ── State: inside a block (variable expression or tag) ────────────────
      if (inBlock > 0) {
        // Bounds-safe backslash skip outside quoted strings.
        if (c == '\\') {
          currPost += (currPost + 1 < length) ? 2 : 1;
          continue;
        }
        // Inside a quoted string: handle escape sequences so a delimiter
        // character that appears as \" or \' does not prematurely close the block.
        if (inQuote != 0) {
          if (c == inQuote) {
            inQuote = 0;
          }
          currPost++;
          continue;
        }
        if (c == '\'' || c == '"') {
          inQuote = c;
          currPost++;
          continue;
        }

        // Check for the closing delimiter matching the current block type.
        char[] closeDelim = closingDelimFor(tokenKind);
        if (closeDelim != null && regionMatches(currPost, closeDelim)) {
          // Emit from the opener start to the end of the closer.
          lastStart = blockOpenerStart;
          tokenLength = currPost + closeDelim.length - blockOpenerStart;
          tokenStart = currPost + closeDelim.length;
          currPost = tokenStart;
          inBlock = 0;
          int kind = tokenKind;
          tokenKind = symbols.getFixed();
          return emitStringToken(kind);
        }
        currPost++;
        continue;
      }

      // ── State: plain text — look for any opening delimiter ────────────────
      if (inRaw == 0) {
        // Variable opener e.g. "{{" or "\VAR{"
        if (regionMatches(currPost, varStart)) {
          Token pending = flushTextBefore(currPost);
          blockOpenerStart = currPost;
          tokenStart = currPost + varStart.length;
          currPost = tokenStart;
          tokenKind = symbols.getExprStart();
          inBlock = 1;
          if (pending != null) {
            return pending;
          }
          continue;
        }
        // Block opener e.g. "{%" or "\BLOCK{"
        if (regionMatches(currPost, blkStart)) {
          Token pending = flushTextBefore(currPost);
          blockOpenerStart = currPost;
          tokenStart = currPost + blkStart.length;
          currPost = tokenStart;
          tokenKind = symbols.getTag();
          inBlock = 1;
          if (pending != null) {
            return pending;
          }
          continue;
        }
        // Comment opener e.g. "{#" or "\#{"
        if (regionMatches(currPost, cmtStart)) {
          Token pending = flushTextBefore(currPost);
          blockOpenerStart = currPost;
          tokenStart = currPost + cmtStart.length;
          currPost = tokenStart;
          tokenKind = symbols.getNote();
          inComment = 1;
          if (pending != null) {
            return pending;
          }
          continue;
        }
      } else {
        // In raw mode: only exit on a block opener immediately followed
        // (after optional whitespace) by "endraw".
        if (regionMatches(currPost, blkStart)) {
          int contentStart = currPost + blkStart.length;
          int pos = contentStart;
          while (pos < length && Character.isWhitespace(is[pos])) {
            pos++;
          }
          if (charArrayRegionMatches(is, pos, "endraw")) {
            Token pending = flushTextBefore(currPost);
            blockOpenerStart = currPost;
            tokenStart = contentStart;
            currPost = tokenStart;
            tokenKind = symbols.getTag();
            inBlock = 1;
            if (pending != null) {
              return pending;
            }
            continue;
          }
        }
      }

      currPost++;
    }

    // End of input: flush any remaining buffered content.
    if (currPost > tokenStart) {
      return getEndTokenStringBased();
    }
    return null;
  }

  /**
   * If {@code is[tokenStart..upTo)} contains un-emitted plain text, captures it
   * as a TEXT token and returns it. Returns {@code null} for zero-length regions.
   *
   * <p>The caller MUST set {@code tokenStart} (and other state) after calling this,
   * regardless of whether a token was returned. This method does NOT update
   * {@code tokenStart} — that would produce the wrong value since the caller needs
   * to set it to just past the opening delimiter.
   */
  private Token flushTextBefore(int upTo) {
    int textLen = upTo - tokenStart;
    if (textLen <= 0) {
      return null;
    }
    lastStart = tokenStart;
    tokenLength = textLen;
    return emitStringToken(symbols.getFixed());
  }

  /** Returns the closing delimiter for the currently open block kind. */
  private char[] closingDelimFor(int currentKind) {
    if (currentKind == symbols.getExprStart()) {
      return varEnd;
    }
    if (currentKind == symbols.getTag()) {
      return blkEnd;
    }
    if (currentKind == symbols.getNote()) {
      return cmtEnd;
    }
    return null;
  }

  /**
   * Constructs a token from {@code lastStart}/{@code tokenLength}, then applies
   * trimBlocks and raw-mode post-processing identical to the char-based path.
   */
  private Token emitStringToken(int kind) {
    Token t = Token.newToken(
      kind,
      symbols,
      whitespaceControlParser,
      String.valueOf(is, lastStart, tokenLength),
      currLine,
      lastStart - lastNewlinePos + 1
    );

    if (
      (t instanceof TagToken || t instanceof NoteToken) &&
      config.isTrimBlocks() &&
      currPost < length &&
      is[currPost] == '\n'
    ) {
      lastNewlinePos = currPost + 1;
      ++currPost;
      ++tokenStart;
    }

    if (t instanceof TagToken) {
      TagToken tt = (TagToken) t;
      if ("raw".equals(tt.getTagName())) {
        inRaw = 1;
        return tt;
      } else if ("endraw".equals(tt.getTagName())) {
        inRaw = 0;
        return tt;
      }
    }

    if (inRaw > 0 && t.getType() != symbols.getFixed()) {
      return Token.newToken(
        symbols.getFixed(),
        symbols,
        whitespaceControlParser,
        t.image,
        currLine,
        lastStart - lastNewlinePos + 1
      );
    }

    return t;
  }

  /**
   * Emits whatever remains at end-of-input.
   *
   * <p>FIX (infinite loop): advances {@code tokenStart = currPost} so that the
   * next call to {@code getNextTokenStringBased()} finds {@code currPost == tokenStart}
   * and returns {@code null} (end of data) instead of re-emitting the same slice.
   */
  private Token getEndTokenStringBased() {
    tokenLength = currPost - tokenStart;
    lastStart = tokenStart;
    tokenStart = currPost; // ← prevents re-emission on subsequent calls
    int type = symbols.getFixed();
    if (inComment > 0) {
      type = symbols.getNote();
    } else if (inBlock > 0) {
      return new UnclosedToken(
        String.valueOf(is, lastStart, tokenLength),
        currLine,
        lastStart - lastNewlinePos + 1,
        symbols,
        whitespaceControlParser
      );
    }
    return Token.newToken(
      type,
      symbols,
      whitespaceControlParser,
      String.valueOf(is, lastStart, tokenLength),
      currLine,
      lastStart - lastNewlinePos + 1
    );
  }

  /** Returns true if {@code is[pos..]} starts with {@code pattern}. */
  private boolean regionMatches(int pos, char[] pattern) {
    if (pos + pattern.length > length) {
      return false;
    }
    for (int i = 0; i < pattern.length; i++) {
      if (is[pos + i] != pattern[i]) {
        return false;
      }
    }
    return true;
  }

  // ── Original char-based scanning path (completely unchanged) ──────────────

  private Token getNextTokenCharBased() {
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
          if (
            config
              .getFeatures()
              .isActive(BuiltInFeatures.WHITESPACE_REQUIRED_WITHIN_TOKENS)
          ) {
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

    if (pos + 5 >= length) {
      return false;
    }

    return charArrayRegionMatches(is, pos - 1, "endraw");
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
        symbols,
        whitespaceControlParser
      );
    }
    return Token.newToken(
      type,
      symbols,
      whitespaceControlParser,
      String.valueOf(is, tokenStart, tokenLength),
      currLine,
      tokenStart - lastNewlinePos + 1
    );
  }

  private Token newToken(int kind) {
    Token t = Token.newToken(
      kind,
      symbols,
      whitespaceControlParser,
      String.valueOf(is, lastStart, tokenLength),
      currLine,
      lastStart - lastNewlinePos + 1
    );

    if (
      (t instanceof TagToken || t instanceof NoteToken) &&
      config.isTrimBlocks() &&
      currPost < length &&
      is[currPost] == '\n'
    ) {
      lastNewlinePos = currPost;
      ++currPost;
      ++tokenStart;
    }

    if (t instanceof TagToken) {
      TagToken tt = (TagToken) t;
      if ("raw".equals(tt.getTagName())) {
        inRaw = 1;
        return tt;
      } else if ("endraw".equals(tt.getTagName())) {
        inRaw = 0;
        return tt;
      }
    }

    if (inRaw > 0 && t.getType() != symbols.getFixed()) {
      return Token.newToken(
        symbols.getFixed(),
        symbols,
        whitespaceControlParser,
        t.image,
        currLine,
        tokenStart
      );
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

  @Override
  protected Token computeNext() {
    Token t = getNextToken();

    if (t == null) {
      return endOfData();
    }

    return t;
  }
}
