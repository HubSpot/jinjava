/**********************************************************************
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
 **********************************************************************/
package com.hubspot.jinjava.tree.parse;

import java.io.Serializable;

public abstract class TokenScannerSymbols implements Serializable {

  private static final long serialVersionUID = -4810220023023256534L;

  private String expressionStart = null;
  private String expressionStartWithTag = null;
  private String openingComment = null;
  private String closingComment = null;
  private String expressionEnd = null;
  private String expressionEndWithTag = null;

  public abstract char getPrefixChar();

  public abstract char getPostfixChar();

  public abstract char getFixedChar();

  public abstract char getNoteChar();

  public abstract char getTagChar();

  public abstract char getExprStartChar();

  public abstract char getExprEndChar();

  public abstract char getNewlineChar();

  public abstract char getTrimChar();

  public int getPrefix() {
    return getPrefixChar();
  }

  public int getPostfix() {
    return getPostfixChar();
  }

  public int getFixed() {
    return getFixedChar();
  }

  public int getNote() {
    return getNoteChar();
  }

  public int getTag() {
    return getTagChar();
  }

  public int getExprStart() {
    return getExprStartChar();
  }

  public int getExprEnd() {
    return getExprEndChar();
  }

  public int getNewline() {
    return getNewlineChar();
  }

  public int getTrim() {
    return getTrimChar();
  }

  public String getExpressionStart() {
    if (expressionStart == null) {
      expressionStart = String.valueOf(getPrefixChar()) + getExprStartChar();
    }
    return expressionStart;
  }

  public String getExpressionEnd() {
    if (expressionEnd == null) {
      expressionEnd = String.valueOf(getExprEndChar()) + getPostfixChar();
    }
    return expressionEnd;
  }

  public String getExpressionStartWithTag() {
    if (expressionStartWithTag == null) {
      expressionStartWithTag = String.valueOf(getPrefixChar()) + getTagChar();
    }
    return expressionStartWithTag;
  }

  public String getExpressionEndWithTag() {
    if (expressionEndWithTag == null) {
      expressionEndWithTag = String.valueOf(getTagChar()) + getPostfixChar();
    }
    return expressionEndWithTag;
  }

  public String getOpeningComment() {
    if (openingComment == null) {
      openingComment = String.valueOf(getPrefixChar()) + getNoteChar();
    }
    return openingComment;
  }

  public String getClosingComment() {
    if (closingComment == null) {
      closingComment = String.valueOf(getNoteChar()) + getPostfixChar();
    }
    return closingComment;
  }

  public static boolean isNoteTagOrExprChar(TokenScannerSymbols symbols, char c) {
    return (
      c == symbols.getNote() || c == symbols.getTag() || c == symbols.getExprStartChar()
    );
  }

  // ── New API ────────────────────────────────────────────────────────────────

  /**
   * Returns {@code true} if this instance uses arbitrary string delimiters that
   * require the string-matching scan path in {@link TokenScanner}.
   *
   * <p>The default returns {@code false}, so all existing subclasses are unaffected.
   * {@link StringTokenScannerSymbols} overrides this to return {@code true}.
   */
  public boolean isStringBased() {
    return false;
  }

  /**
   * Length of the variable/expression opening delimiter (e.g. 2 for {@code "{{"}),
   * used by {@link ExpressionToken#parse()} instead of the hardcoded constant 2.
   */
  public int getExpressionStartLength() {
    return getExpressionStart().length();
  }

  /**
   * Length of the variable/expression closing delimiter (e.g. 2 for {@code "}}"}),
   * used by {@link ExpressionToken#parse()} instead of the hardcoded constant 2.
   */
  public int getExpressionEndLength() {
    return getExpressionEnd().length();
  }

  /**
   * Length of the block/tag opening delimiter (e.g. 2 for {@code "{%"}),
   * used by {@link TagToken#parse()} instead of the hardcoded constant 2.
   */
  public int getTagStartLength() {
    return getExpressionStartWithTag().length();
  }

  /**
   * Length of the block/tag closing delimiter (e.g. 2 for {@code "%}"}),
   * used by {@link TagToken#parse()} instead of the hardcoded constant 2.
   */
  public int getTagEndLength() {
    return getExpressionEndWithTag().length();
  }

  /**
   * Length of the comment opening delimiter (e.g. 2 for {@code "{#"}),
   * used by {@link NoteToken#parse()} instead of the hardcoded constant 2.
   */
  public int getCommentStartLength() {
    return getOpeningComment().length();
  }

  /**
   * Length of the comment closing delimiter (e.g. 2 for {@code "#}"}),
   * used by {@link NoteToken#parse()} instead of the hardcoded constant 2.
   */
  public int getCommentEndLength() {
    return getClosingComment().length();
  }

  /**
   * Optional line statement prefix (e.g. {@code "%%"}). When non-null, any line
   * that begins with this prefix (after optional horizontal whitespace) is treated
   * as a block tag statement, equivalent to wrapping its content in the block
   * delimiters. Returns {@code null} by default (feature disabled).
   *
   * <p>Only used by {@link StringTokenScannerSymbols}; has no effect in the
   * char-based path.
   */
  public String getLineStatementPrefix() {
    return null;
  }

  /**
   * Optional line comment prefix (e.g. {@code "%#"}). When non-null, any line
   * that begins with this prefix (after optional horizontal whitespace) is stripped
   * entirely from the output. Returns {@code null} by default (feature disabled).
   *
   * <p>Only used by {@link StringTokenScannerSymbols}; has no effect in the
   * char-based path.
   */
  public String getLineCommentPrefix() {
    return null;
  }
}
