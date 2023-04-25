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
}
