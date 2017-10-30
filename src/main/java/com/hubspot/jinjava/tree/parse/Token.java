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

import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_EXPR_START;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_FIXED;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_NOTE;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_TAG;

import java.io.Serializable;

import com.hubspot.jinjava.interpret.UnexpectedTokenException;

public abstract class Token implements Serializable {

  private static final long serialVersionUID = 3359084948763661809L;

  protected final String image;
  // useful for some token type
  protected String content;

  protected final int lineNumber;
  protected final int startPosition;

  private boolean leftTrim;
  private boolean rightTrim;
  private boolean rightTrimAfterEnd;

  public Token(String image, int lineNumber, int startPosition) {
    this.image = image;
    this.lineNumber = lineNumber;
    this.startPosition = startPosition;
    parse();
  }

  public Token(String image, int lineNumber) {
    this(image, lineNumber, -1);
  }

  public String getImage() {
    return image;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public boolean isLeftTrim() {
    return leftTrim;
  }

  public boolean isRightTrim() {
    return rightTrim;
  }

  public boolean isRightTrimAfterEnd() {
    return rightTrimAfterEnd;
  }

  public void setLeftTrim(boolean leftTrim) {
    this.leftTrim = leftTrim;
  }

  public void setRightTrim(boolean rightTrim) {
    this.rightTrim = rightTrim;
  }

  public void setRightTrimAfterEnd(boolean rightTrimAfterEnd) {
    this.rightTrimAfterEnd = rightTrimAfterEnd;
  }

  public int getStartPosition() {
    return startPosition;
  }

  @Override
  public String toString() {
    return image;
  }

  protected abstract void parse();

  public abstract int getType();

  static Token newToken(int tokenKind, String image, int lineNumber, int startPosition) {
    switch (tokenKind) {
    case TOKEN_FIXED:
      return new TextToken(image, lineNumber, startPosition);
    case TOKEN_NOTE:
      return new NoteToken(image, lineNumber, startPosition);
    case TOKEN_EXPR_START:
      return new ExpressionToken(image, lineNumber, startPosition);
    case TOKEN_TAG:
      return new TagToken(image, lineNumber, startPosition);
    default:
      throw new UnexpectedTokenException(String.valueOf((char) tokenKind), lineNumber, startPosition);
    }
  }

}
