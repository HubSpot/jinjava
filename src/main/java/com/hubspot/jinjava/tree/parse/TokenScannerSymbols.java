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

  public abstract char getTokenPrefixChar();

  public abstract char getTokenPostfixChar();

  public abstract char getTokenFixedChar();

  public abstract char getTokenNoteChar();

  public abstract char getTokenTagChar();

  public abstract char getTokenExprStartChar();

  public abstract char getTokenExprEndChar();

  public abstract char getTokenNewlineChar();

  public abstract char getTokenTrimChar();

  public int getTokenPrefix() {
    return getTokenPrefixChar();
  }

  public int getTokenPostfix() {
    return getTokenPostfixChar();
  }

  public int getTokenFixed() {
    return getTokenFixedChar();
  }

  public int getTokenNote() {
    return getTokenNoteChar();
  }

  public int getTokenTag() {
    return getTokenTagChar();
  }

  public int getTokenExprStart() {
    return getTokenExprStartChar();
  }

  public int getTokenExprEnd() {
    return getTokenExprEndChar();
  }

  public int getTokenNewline() {
    return getTokenNewlineChar();
  }

  public int getTokenTrim() {
    return getTokenTrimChar();
  }
}
