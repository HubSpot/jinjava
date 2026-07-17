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

/**
 * String-matching token scanner for {@link TokenScannerSymbols} implementations
 * where {@link TokenScannerSymbols#isStringBased()} returns {@code true} — most
 * notably {@link StringTokenScannerSymbols}.
 *
 * <p>Unlike the character-based {@link TokenScanner}, this scanner matches
 * multi-character delimiter strings directly (e.g. {@code \VAR{} / {@code }},
 * {@code \BLOCK{} / {@code }}) without relying on a shared prefix character. It also
 * supports optional {@link TokenScannerSymbols#getLineStatementPrefix() line statement}
 * and {@link TokenScannerSymbols#getLineCommentPrefix() line comment} prefixes,
 * matching Python Jinja2 semantics.
 *
 * <p>{@link TreeParser} selects this scanner automatically when
 * {@code symbols.isStringBased()} is {@code true}; callers never instantiate it
 * directly.
 */
public class StringTokenScanner extends AbstractIterator<Token> {

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

  private final char[] varStart;
  private final char[] varEnd;
  private final char[] blkStart;
  private final char[] blkEnd;
  private final char[] cmtStart;
  private final char[] cmtEnd;

  // Optional line-oriented prefixes; null when not configured.
  private final char[] lineStmtPrefix;
  private final char[] lineCommentPrefix;

  // When true, backslash is treated as an escape character only inside quoted
  // string literals, matching Jinja2 behaviour. When false (legacy default),
  // the scanner consumes backslash + next char unconditionally.
  private final boolean backslashInQuotesOnly;

  // Remembers where the current opening delimiter began so the emitted block/comment
  // token image starts from the opener (not the content), letting parse() strip the
  // correct number of delimiter characters from both ends.
  private int blockOpenerStart = 0;

  public StringTokenScanner(String input, JinjavaConfig config) {
    this.config = config;

    is = input.toCharArray();
    length = is.length;

    symbols = config.getTokenScannerSymbols();
    whitespaceControlParser =
      config.getLegacyOverrides().isParseWhitespaceControlStrictly()
        ? WhitespaceControlParser.STRICT
        : WhitespaceControlParser.LENIENT;

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

    backslashInQuotesOnly = config.getLegacyOverrides().isHandleBackslashInQuotesOnly();
  }

  // ── Core scanning loop ────────────────────────────────────────────────────
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

  private Token getNextToken() {
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
      return getEndToken();
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
      return emitToken(kind);
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
      // Inside a quoted string: a backslash always escapes the next character.
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
    // Outside a quoted string: only consume the backslash if the legacy
    // flag is enabled; otherwise leave it for the expression parser.
    if (c == '\\' && !backslashInQuotesOnly) {
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
      return emitToken(kind);
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
    // Line comments match anywhere on a line, not just at the start.
    if (lineCommentPrefix != null && regionMatches(currPost, lineCommentPrefix)) {
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

    // When lstrip_blocks is active, Python Jinja2 also consumes any blank lines
    // that follow a line statement (lines containing only horizontal whitespace).
    // This prevents blank lines between consecutive line statements from
    // appearing in the output.
    if (config.isLstripBlocks()) {
      while (next < length) {
        // Scan forward past any horizontal whitespace on this line.
        int lineEnd = next;
        while (
          lineEnd < length &&
          is[lineEnd] != '\n' &&
          (is[lineEnd] == ' ' || is[lineEnd] == '\t')
        ) {
          lineEnd++;
        }
        // If we hit a newline (blank or whitespace-only line), consume it.
        if (lineEnd < length && is[lineEnd] == '\n') {
          next = lineEnd + 1;
          currLine++;
          lastNewlinePos = next;
        } else {
          // Hit real content or end of input — stop consuming.
          break;
        }
      }
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
   * <p>Line comments match anywhere on a line (not just at the start).
   * For mid-line comments, everything from the prefix to end of line is
   * stripped; the text before the prefix on the same line is kept.
   *
   * <p>Confirmed Python Jinja2 semantics:
   * <ul>
   *   <li><b>Plain {@code %#}</b>: comment content stripped, own trailing
   *       {@code \n} kept. Replaces the comment (and anything after it on
   *       the line) with a blank line / line ending.</li>
   *   <li><b>{@code %#-} at start of line</b>: also strips preceding blank
   *       lines and the {@code \n} ending the last real-content line.</li>
   *   <li><b>{@code %#-} mid-line</b>: behaves like plain {@code %#} — the
   *       {@code -} has nothing to left-trim when real content precedes it.</li>
   * </ul>
   */
  private Token handleLineComment() {
    boolean startOfLine = isStartOfLine(currPost);
    int afterPrefix = currPost + lineCommentPrefix.length;
    boolean hasTrimModifier =
      afterPrefix < length && is[afterPrefix] == symbols.getTrimChar();

    int flushUpTo;
    if (!startOfLine) {
      // Mid-line comment: flush up to the %# prefix, stripping trailing
      // horizontal whitespace before it (Python strips spaces/tabs before
      // mid-line comments, e.g. "hello %# comment" → "hello").
      int p = currPost - 1;
      while (p >= tokenStart && (is[p] == ' ' || is[p] == '\t')) {
        p--;
      }
      flushUpTo = p + 1;
    } else if (hasTrimModifier) {
      // Start-of-line %#-: strip preceding blank lines and the real-content \n.
      flushUpTo = lineIndentStartSkippingBlanks(currPost);
    } else {
      // Start-of-line %#: strip only the current line's indentation.
      flushUpTo = lineIndentStart(currPost);
    }

    Token pending = flushTextBefore(flushUpTo);

    // Advance past the comment content to the end of the line.
    int end = afterPrefix;
    while (end < length && is[end] != '\n') {
      end++;
    }

    // Both %# and %#- keep the trailing \n — it appears in the output.
    tokenStart = end;
    currPost = end;

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

  /**
   * Returns the flush boundary for a {@code %#-} line comment.
   *
   * <p>Python Jinja2 semantics for {@code %#-}: strip back through any preceding
   * blank lines AND the {@code \n} that ends the last real-content line, so that
   * the comment's own kept {@code \n} becomes the sole separator. Stops at
   * {@code tokenStart} so that {@code \n}s produced by preceding line statements
   * or plain {@code %#} comments are not consumed.
   *
   * <p>Examples (| marks the flush boundary):
   * <pre>
   *   "A\n\n%#-"   →  flush "A|"      → output "A" + comment's \n
   *   "%% set\n%#-" → flush nothing    → output comment's \n  (tokenStart guard)
   * </pre>
   */
  private int lineIndentStartSkippingBlanks(int pos) {
    int p = pos - 1;
    while (p >= tokenStart) {
      // Skip trailing horizontal whitespace on this line (going backwards).
      while (p >= tokenStart && (is[p] == ' ' || is[p] == '\t')) {
        p--;
      }
      if (p < tokenStart) {
        break;
      }
      if (is[p] == '\n') {
        // Blank line — consume this \n and keep scanning backwards.
        p--;
      } else {
        // Real content at position p. The \n ending this line is at p+1.
        // Return p+1 so flushTextBefore(p+1) flushes up to but NOT including
        // that \n, stripping it from the output.
        return p + 1;
      }
    }
    // Reached tokenStart without finding real content — all blank lines were
    // preceded by a line statement or plain comment. Preserve them.
    return tokenStart;
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
    return emitToken(symbols.getFixed());
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
  private Token emitToken(int kind) {
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
  private Token getEndToken() {
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
}
