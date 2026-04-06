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

  // Optional line-oriented prefixes; null when not configured.
  private final char[] lineStmtPrefix;
  private final char[] lineCommentPrefix;

  // Remembers where the current opening delimiter began so the emitted block/comment
  // token image starts from the opener (not the content), letting parse() strip the
  // correct number of delimiter characters from both ends.
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

      String lsp = symbols.getLineStatementPrefix();
      lineStmtPrefix = (lsp != null && !lsp.isEmpty()) ? lsp.toCharArray() : null;

      String lcp = symbols.getLineCommentPrefix();
      lineCommentPrefix = (lcp != null && !lcp.isEmpty()) ? lcp.toCharArray() : null;
    } else {
      varStart = varEnd = blkStart = blkEnd = cmtStart = cmtEnd = null;
      lineStmtPrefix = null;
      lineCommentPrefix = null;
    }
  }

  // ── Dispatch ───────────────────────────────────────────────────────────────

  private Token getNextToken() {
    return stringBased ? getNextTokenStringBased() : getNextTokenCharBased();
  }

  // ── String-based scanning path ─────────────────────────────────────────────
  //
  // tokenStart       — start of the next text region to buffer.
  // blockOpenerStart — position of the current opening delimiter; the emitted
  //                    block/comment token image begins here.
  // lastStart / tokenLength — the slice passed to Token.newToken().
  //
  // Two-phase emission:
  //   1. Opener detected → flush buffered plain text as TEXT, record
  //      blockOpenerStart, advance tokenStart/currPost past the opener into
  //      the block content, set inBlock/inComment.
  //   2. Closer detected → emit is[blockOpenerStart .. closerEnd) as the
  //      appropriate token type; advance tokenStart = currPost = closerEnd.

  // Sentinel returned by scan helpers to mean "a delimiter was matched and
  // scanner state was updated — loop again without advancing currPost".
  // Any non-null return from a helper that is NOT this sentinel is a real token.
  private static final Token DELIMITER_MATCHED = new TextToken(
    "",
    0,
    0,
    new DefaultTokenScannerSymbols()
  );

  private Token getNextTokenStringBased() {
    while (currPost < length) {
      char c = is[currPost];

      if (c == '\n') {
        currLine++;
        lastNewlinePos = currPost + 1;
      }

      if (inComment > 0) {
        Token t = scanInsideComment();
        if (t != null) {
          return t;
        }
        continue; // scanInsideComment advanced currPost
      }

      if (inBlock > 0) {
        Token t = scanInsideBlock(c);
        if (t == DELIMITER_MATCHED) {
          continue; // closer not yet found, currPost already advanced
        }
        if (t != null) {
          return t;
        }
        continue;
      }

      if (inRaw == 0) {
        Token t = scanPlainText(c);
        if (t == DELIMITER_MATCHED) {
          continue; // opener matched, state updated, no pending text
        }
        if (t != null) {
          return t; // pending text flushed, or line-statement token
        }
        // null means nothing matched — fall through to advance
      } else {
        Token t = scanRawMode();
        if (t == DELIMITER_MATCHED) {
          continue;
        }
        if (t != null) {
          return t;
        }
      }

      currPost++;
    }

    if (currPost > tokenStart) {
      return getEndTokenStringBased();
    }
    return null;
  }

  /** Scans one character while inside a comment block; advances {@code currPost}. */
  private Token scanInsideComment() {
    if (regionMatches(currPost, cmtEnd)) {
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
    return null;
  }

  /**
   * Scans one character while inside a variable or tag block; advances
   * {@code currPost}. Returns a real token when the closer is found, or
   * {@link #DELIMITER_MATCHED} (meaning "keep looping") otherwise.
   */
  private Token scanInsideBlock(char c) {
    if (inQuote != 0) {
      // Inside a quoted string: a backslash escapes the next character so a
      // delimiter or quote character following it does not prematurely close
      // the block or the string.
      if (c == '\\') {
        currPost += (currPost + 1 < length) ? 2 : 1;
        return DELIMITER_MATCHED;
      }
      if (c == inQuote) {
        inQuote = 0;
      }
      currPost++;
      return DELIMITER_MATCHED;
    }
    // Outside a quoted string: a backslash escapes the next character.
    if (c == '\\') {
      currPost += (currPost + 1 < length) ? 2 : 1;
      return DELIMITER_MATCHED;
    }
    if (c == '\'' || c == '"') {
      inQuote = c;
      currPost++;
      return DELIMITER_MATCHED;
    }
    // Check for the closing delimiter matching the current block type.
    char[] closeDelim = closingDelimFor(tokenKind);
    if (closeDelim != null && regionMatches(currPost, closeDelim)) {
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
    return DELIMITER_MATCHED;
  }

  /**
   * Scans for openers while in normal (non-raw) plain-text mode.
   * Returns a real token when one is ready to emit, {@link #DELIMITER_MATCHED}
   * when an opener was matched with no pending text, or {@code null} when
   * nothing matched (caller should advance {@code currPost}).
   */
  private Token scanPlainText(char c) {
    // ── Line statement prefix (e.g. "%% if foo") ──────────────────────────
    if (
      lineStmtPrefix != null &&
      isStartOfLine(currPost) &&
      regionMatches(currPost, lineStmtPrefix)
    ) {
      return handleLineStatement();
    }
    // ── Line comment prefix (e.g. "%# this is ignored") ───────────────────
    if (
      lineCommentPrefix != null &&
      isStartOfLine(currPost) &&
      regionMatches(currPost, lineCommentPrefix)
    ) {
      return handleLineComment();
    }
    // ── Variable opener e.g. "{{" or "\VAR{" ──────────────────────────────
    if (regionMatches(currPost, varStart)) {
      return openBlock(varStart, symbols.getExprStart(), false);
    }
    // ── Block opener e.g. "{%" or "\BLOCK{" ───────────────────────────────
    if (regionMatches(currPost, blkStart)) {
      return openBlock(blkStart, symbols.getTag(), false);
    }
    // ── Comment opener e.g. "{#" or "\#{" ─────────────────────────────────
    if (regionMatches(currPost, cmtStart)) {
      return openBlock(cmtStart, symbols.getNote(), true);
    }
    return null; // nothing matched
  }

  /**
   * Scans for the endraw block opener while in raw mode.
   * Returns a real token, {@link #DELIMITER_MATCHED}, or {@code null}.
   */
  private Token scanRawMode() {
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
        return DELIMITER_MATCHED;
      }
    }
    return null;
  }

  /**
   * Opens a variable or tag block (sets {@code inBlock}) or a comment block
   * (sets {@code inComment}). Flushes any pending text first.
   * Returns the pending text token if one exists, {@link #DELIMITER_MATCHED} otherwise.
   */
  private Token openBlock(char[] opener, int kind, boolean isComment) {
    Token pending = flushTextBefore(currPost);
    blockOpenerStart = currPost;
    tokenStart = currPost + opener.length;
    currPost = tokenStart;
    tokenKind = kind;
    if (isComment) {
      inComment = 1;
    } else {
      inBlock = 1;
    }
    return (pending != null) ? pending : DELIMITER_MATCHED;
  }

  /**
   * Handles a line statement prefix: consumes the line, builds a synthetic block
   * tag token, and returns appropriately (stashing the tag if text was pending).
   */
  private Token handleLineStatement() {
    Token pending = flushTextBefore(lineIndentStart(currPost));

    int contentStart = currPost + lineStmtPrefix.length;
    while (contentStart < length && is[contentStart] == ' ') {
      contentStart++;
    }
    int contentEnd = contentStart;
    while (contentEnd < length && is[contentEnd] != '\n') {
      contentEnd++;
    }
    // Do NOT trim inner here — TagToken.parse() calls handleTrim() which detects
    // a leading '-' for left-trim whitespace control and a trailing '-' for
    // right-trim. Trimming here would strip those control characters before
    // TagToken ever sees them.
    // Also do not insert a space before the content when it starts with the
    // trim char '-', as that space would prevent handleTrim from detecting it.
    String inner = String.valueOf(is, contentStart, contentEnd - contentStart);
    String prefix = (inner.length() > 0 && inner.charAt(0) == symbols.getTrimChar())
      ? symbols.getExpressionStartWithTag()
      : symbols.getExpressionStartWithTag() + " ";
    String syntheticImage = prefix + inner + " " + symbols.getExpressionEndWithTag();

    int next = contentEnd;
    if (next < length && is[next] == '\n') {
      next++;
      currLine++;
      lastNewlinePos = next;
    }
    tokenStart = next;
    currPost = next;

    Token stmtToken = Token.newToken(
      symbols.getTag(),
      symbols,
      whitespaceControlParser,
      syntheticImage,
      currLine,
      1
    );
    if (pending != null) {
      pendingToken = stmtToken;
      return pending;
    }
    return stmtToken;
  }

  /**
   * Handles a line comment prefix.
   *
   * <p>Matches Python Jinja2 semantics exactly:
   * <ul>
   *   <li><b>Plain {@code %#}</b>: the comment content is stripped but the line's
   *       trailing {@code \n} is <em>kept</em>. The comment line is effectively
   *       replaced by a blank line in the output.</li>
   *   <li><b>{@code %#-} (trim modifier)</b>: the comment content AND its trailing
   *       {@code \n} are both stripped, leaving no blank line.</li>
   * </ul>
   *
   * <p>Neither form affects the newline that ended the <em>preceding</em> line.
   */
  private Token handleLineComment() {
    int afterPrefix = currPost + lineCommentPrefix.length;
    boolean hasTrimModifier =
      afterPrefix < length && is[afterPrefix] == symbols.getTrimChar();

    // Flush buffered text up to (but not including) the current line's indentation.
    // The preceding newline is always preserved regardless of the trim modifier.
    Token pending = flushTextBefore(lineIndentStart(currPost));

    // Advance past the comment content to the end of the line.
    int end = afterPrefix;
    while (end < length && is[end] != '\n') {
      end++;
    }

    if (hasTrimModifier) {
      // %#- : strip trailing \n too, leaving no blank line.
      int next = end;
      if (next < length && is[next] == '\n') {
        next++;
        currLine++;
        lastNewlinePos = next;
      }
      tokenStart = next;
      currPost = next;
    } else {
      // %# : leave the trailing \n in place so it renders as a blank line.
      tokenStart = end;
      currPost = end;
    }

    return (pending != null) ? pending : DELIMITER_MATCHED;
  }

  /**
   * Returns the position of the first character of the indentation on the line
   * containing {@code pos} — i.e. the position just after the preceding newline
   * (or 0 if at the start of input). Used to exclude leading horizontal whitespace
   * from the text token flushed before a line prefix match.
   */
  private int lineIndentStart(int pos) {
    int p = pos - 1;
    while (p >= 0 && (is[p] == ' ' || is[p] == '\t')) {
      p--;
    }
    // p is now at the newline before the indentation, or at -1.
    return p + 1;
  }

  // ── One-slot stash for the synthetic tag after a line-statement ─────────
  // When a line-statement prefix is found and there is pending text to flush
  // first, we return the text token immediately and stash the synthetic tag
  // here so computeNext() picks it up on the very next call.
  private Token pendingToken = null;

  @Override
  protected Token computeNext() {
    // Drain any stashed token first.
    if (pendingToken != null) {
      Token t = pendingToken;
      pendingToken = null;
      return t;
    }

    Token t = getNextToken();
    if (t == null) {
      return endOfData();
    }
    return t;
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /**
   * Returns true when {@code pos} is at the start of a line — i.e. it is either
   * the very first character of the input, or the character immediately after a
   * newline (accounting for any leading whitespace that lstripBlocks may allow).
   */
  private boolean isStartOfLine(int pos) {
    if (pos == 0) {
      return true;
    }
    // Walk backwards past any horizontal whitespace (spaces/tabs).
    int p = pos - 1;
    while (p >= 0 && (is[p] == ' ' || is[p] == '\t')) {
      p--;
    }
    // True if we hit the beginning of the input or a newline.
    return p < 0 || is[p] == '\n';
  }

  /**
   * If {@code is[tokenStart..upTo)} contains un-emitted plain text, captures it
   * as a TEXT token and returns it. Returns {@code null} for zero-length regions.
   * Does NOT update {@code tokenStart} — the caller sets it after returning.
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
   * Advances {@code tokenStart = currPost} so subsequent calls return null.
   */
  private Token getEndTokenStringBased() {
    tokenLength = currPost - tokenStart;
    lastStart = tokenStart;
    tokenStart = currPost;
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
}
