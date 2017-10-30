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

import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_FIXED;

import org.apache.commons.lang3.StringUtils;

public class TextToken extends Token {

  private static final long serialVersionUID = -6168990984496468543L;

  public TextToken(String image, int lineNumber, int startPosition) {
    super(image, lineNumber, startPosition);
  }

  @Override
  public int getType() {
    return TOKEN_FIXED;
  }

  @Override
  protected void parse() {
    content = image;
  }

  public boolean isBlank() {
    return StringUtils.isBlank(content);
  }

  public String trim() {
    return content.trim();
  }

  public String output() {

    if (isLeftTrim() && isRightTrim()) {
      return trim();
    } else if (isLeftTrim()) {
      return StringUtils.stripStart(content, null);
    } else if (isRightTrim()) {
      return StringUtils.stripEnd(content, null);
    }
    return content;
  }

  @Override
  public String toString() {
    if (isBlank()) {
      return "{~ ~}";
    }
    return "{~ " + content + " ~}";
  }
}
