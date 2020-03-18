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

public abstract class TokenScannerSymbols {
  private String expressionStart = null;
  private String expressionStartWithTag = null;
  private String closingComment = null;

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
      expressionStart =
        new StringBuilder().append(getPrefixChar()).append(getExprStartChar()).toString();
    }
    return expressionStart;
  }

  public String getExpressionStartWithTag() {
    if (expressionStartWithTag == null) {
      expressionStartWithTag =
        new StringBuilder().append(getPrefixChar()).append(getTagChar()).toString();
    }
    return expressionStartWithTag;
  }

  public String getClosingComment() {
    if (closingComment == null) {
      closingComment =
        new StringBuilder().append(getNoteChar()).append(getPostfixChar()).toString();
    }
    return closingComment;
  }
}
