/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.tree.parse;

/**
 * A {@link TokenScannerSymbols} implementation that supports arbitrary multi-character
 * delimiter strings, addressing
 * <a href="https://github.com/HubSpot/jinjava/issues/195">issue #195</a>.
 *
 * <p>Unlike {@link DefaultTokenScannerSymbols}, which is constrained to single-character
 * prefixes and postfixes, this class allows any non-empty string for each of the six
 * delimiter roles. The delimiters do not need to share a common prefix character.
 *
 * <p>{@link TokenScanner} detects this class via {@link #isStringBased()} and activates
 * a string-matching scan path. {@link ExpressionToken}, {@link TagToken}, and
 * {@link NoteToken} use the length accessors on {@link TokenScannerSymbols} (e.g.
 * {@link #getExpressionStartLength()}) to strip delimiters correctly regardless of length.
 *
 * <p>The single-character abstract methods inherited from {@link TokenScannerSymbols}
 * return private Unicode Private-Use-Area sentinel values. These are used only as
 * token-kind discriminators inside {@link Token#newToken} and must never be used for
 * scanning template text.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * JinjavaConfig config = JinjavaConfig.newBuilder()
 *     .withTokenScannerSymbols(StringTokenScannerSymbols.builder()
 *         .withVariableStartString("\\VAR{")
 *         .withVariableEndString("}")
 *         .withBlockStartString("\\BLOCK{")
 *         .withBlockEndString("}")
 *         .withCommentStartString("\\#{")
 *         .withCommentEndString("}")
 *         .build())
 *     .build();
 * }</pre>
 */
public class StringTokenScannerSymbols extends TokenScannerSymbols {

  private static final long serialVersionUID = 1L;

  // ── Internal sentinel chars ────────────────────────────────────────────────
  // Unicode Private Use Area values — guaranteed never to appear in real template
  // text, so Token.newToken()'s if-chain dispatches to the right Token subclass.
  static final char SENTINEL_FIXED = '\uE000';
  static final char SENTINEL_NOTE = '\uE001';
  static final char SENTINEL_TAG = '\uE002';
  static final char SENTINEL_EXPR_START = '\uE003';
  static final char SENTINEL_EXPR_END = '\uE004';
  static final char SENTINEL_PREFIX = '\uE005'; // unused for scanning
  static final char SENTINEL_POSTFIX = '\uE006'; // unused for scanning
  static final char SENTINEL_NEWLINE = '\n'; // real newline for line tracking
  static final char SENTINEL_TRIM = '-'; // real trim char

  // ── The configured string delimiters ──────────────────────────────────────
  private final String variableStartString;
  private final String variableEndString;
  private final String blockStartString;
  private final String blockEndString;
  private final String commentStartString;
  private final String commentEndString;
  // Optional; null means disabled.
  private final String lineStatementPrefix;
  private final String lineCommentPrefix;

  private StringTokenScannerSymbols(Builder builder) {
    this.variableStartString = builder.variableStartString;
    this.variableEndString = builder.variableEndString;
    this.blockStartString = builder.blockStartString;
    this.blockEndString = builder.blockEndString;
    this.commentStartString = builder.commentStartString;
    this.commentEndString = builder.commentEndString;
    this.lineStatementPrefix = builder.lineStatementPrefix;
    this.lineCommentPrefix = builder.lineCommentPrefix;
  }

  // ── Abstract char contract — returns sentinels only ───────────────────────

  @Override
  public char getPrefixChar() {
    return SENTINEL_PREFIX;
  }

  @Override
  public char getPostfixChar() {
    return SENTINEL_POSTFIX;
  }

  @Override
  public char getFixedChar() {
    return SENTINEL_FIXED;
  }

  @Override
  public char getNoteChar() {
    return SENTINEL_NOTE;
  }

  @Override
  public char getTagChar() {
    return SENTINEL_TAG;
  }

  @Override
  public char getExprStartChar() {
    return SENTINEL_EXPR_START;
  }

  @Override
  public char getExprEndChar() {
    return SENTINEL_EXPR_END;
  }

  @Override
  public char getNewlineChar() {
    return SENTINEL_NEWLINE;
  }

  @Override
  public char getTrimChar() {
    return SENTINEL_TRIM;
  }

  // ── String-level getters: MUST override the base-class lazy cache ──────────
  // The base class builds these from the char methods above, which would produce
  // garbage sentinel strings. We override them to return the real delimiters so
  // that ExpressionToken, TagToken, and NoteToken strip content correctly.

  @Override
  public String getExpressionStart() {
    return variableStartString;
  }

  @Override
  public String getExpressionEnd() {
    return variableEndString;
  }

  @Override
  public String getExpressionStartWithTag() {
    return blockStartString;
  }

  @Override
  public String getExpressionEndWithTag() {
    return blockEndString;
  }

  @Override
  public String getOpeningComment() {
    return commentStartString;
  }

  @Override
  public String getClosingComment() {
    return commentEndString;
  }

  @Override
  public String getLineStatementPrefix() {
    return lineStatementPrefix;
  }

  @Override
  public String getLineCommentPrefix() {
    return lineCommentPrefix;
  }

  // ── isStringBased flag ────────────────────────────────────────────────────

  @Override
  public boolean isStringBased() {
    return true;
  }

  // ── Builder ────────────────────────────────────────────────────────────────

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    // Defaults mirror the standard Jinja2 delimiters, so building with no
    // overrides behaves identically to DefaultTokenScannerSymbols.
    private String variableStartString = "{{";
    private String variableEndString = "}}";
    private String blockStartString = "{%";
    private String blockEndString = "%}";
    private String commentStartString = "{#";
    private String commentEndString = "#}";
    private String lineStatementPrefix = null; // disabled by default
    private String lineCommentPrefix = null; // disabled by default

    public Builder withVariableStartString(String s) {
      this.variableStartString = requireNonEmpty(s, "variableStartString");
      return this;
    }

    public Builder withVariableEndString(String s) {
      this.variableEndString = requireNonEmpty(s, "variableEndString");
      return this;
    }

    public Builder withBlockStartString(String s) {
      this.blockStartString = requireNonEmpty(s, "blockStartString");
      return this;
    }

    public Builder withBlockEndString(String s) {
      this.blockEndString = requireNonEmpty(s, "blockEndString");
      return this;
    }

    public Builder withCommentStartString(String s) {
      this.commentStartString = requireNonEmpty(s, "commentStartString");
      return this;
    }

    public Builder withCommentEndString(String s) {
      this.commentEndString = requireNonEmpty(s, "commentEndString");
      return this;
    }

    /**
     * Sets the line statement prefix (e.g. {@code "%%"}). A line beginning with
     * this prefix is treated as a block tag, equivalent to wrapping its content
     * in the configured block delimiters. Pass {@code null} to disable (default).
     */
    public Builder withLineStatementPrefix(String s) {
      this.lineStatementPrefix = s;
      return this;
    }

    /**
     * Sets the line comment prefix (e.g. {@code "%#"}). A line beginning with
     * this prefix is stripped entirely from the output. Pass {@code null} to
     * disable (default).
     */
    public Builder withLineCommentPrefix(String s) {
      this.lineCommentPrefix = s;
      return this;
    }

    public StringTokenScannerSymbols build() {
      return new StringTokenScannerSymbols(this);
    }

    private static String requireNonEmpty(String value, String name) {
      if (value == null || value.isEmpty()) {
        throw new IllegalArgumentException(name + " must not be null or empty");
      }
      return value;
    }
  }
}
