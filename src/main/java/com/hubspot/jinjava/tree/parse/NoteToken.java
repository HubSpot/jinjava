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

import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_NOTE;

public class NoteToken extends Token {

  private static final long serialVersionUID = -3859011447900311329L;

  public NoteToken(String image, int lineNumber, int startPosition) {
    super(image, lineNumber, startPosition);
  }

  @Override
  public int getType() {
    return TOKEN_NOTE;
  }

  /**
   * remove all content, we don't need it.
   */
  @Override
  protected void parse() {
    content = "";
  }

  @Override
  public String toString() {
    return "{# ----comment---- #}";
  }

}
