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

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnexpectedTokenException;
import java.io.Serializable;

public abstract class Token implements Serializable {
  private static final long serialVersionUID = 3359084948763661809L;

  protected String image;
  // useful for some token type
  protected String content;

  protected final int lineNumber;
  protected final int startPosition;
  private final TokenScannerSymbols symbols;

  private boolean leftTrim;
  private boolean rightTrim;
  private boolean rightTrimAfterEnd;

  public Token(
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols
  ) {
    this.image = image;
    this.lineNumber = lineNumber;
    this.startPosition = startPosition;
    this.symbols = symbols;
    parse();
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

  /**
   * Handle any whitespace control characters, capturing whether leading or trailing
   * whitespace should be stripped.
   * @param unwrapped the content of the block stripped of its delimeters
   * @return the content stripped of any whitespace control characters.
   */
  protected final String handleTrim(String unwrapped) {
    boolean parseWhitespaceControlStrictly = JinjavaInterpreter
      .getCurrentMaybe()
      .map(JinjavaInterpreter::getConfig)
      .map(JinjavaConfig::getLegacyOverrides)
      .map(LegacyOverrides::isParseWhitespaceControlStrictly)
      .orElse(false);

    WhitespaceControlParser parser = parseWhitespaceControlStrictly
      ? WhitespaceControlParser.STRICT
      : WhitespaceControlParser.LENIENT;

    String result = unwrapped;
    if (parser.hasLeftTrim(result)) {
      setLeftTrim(true);
      result = parser.stripLeft(result);
    }
    if (parser.hasRightTrim(result)) {
      setRightTrim(true);
      result = parser.stripRight(result);
    }
    return result;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public TokenScannerSymbols getSymbols() {
    return symbols;
  }

  @Override
  public String toString() {
    return image;
  }

  protected abstract void parse();

  public abstract int getType();

  static Token newToken(
    int tokenKind,
    TokenScannerSymbols symbols,
    String image,
    int lineNumber,
    int startPosition
  ) {
    if (tokenKind == symbols.getFixed()) {
      return new TextToken(image, lineNumber, startPosition, symbols);
    } else if (tokenKind == symbols.getNote()) {
      return new NoteToken(image, lineNumber, startPosition, symbols);
    } else if (tokenKind == symbols.getExprStart()) {
      return new ExpressionToken(image, lineNumber, startPosition, symbols);
    } else if (tokenKind == symbols.getTag()) {
      return new TagToken(image, lineNumber, startPosition, symbols);
    } else {
      throw new UnexpectedTokenException(
        String.valueOf((char) tokenKind),
        lineNumber,
        startPosition
      );
    }
  }
}
