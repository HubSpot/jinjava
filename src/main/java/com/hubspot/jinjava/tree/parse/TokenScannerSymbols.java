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
  
  public abstract char TOKEN_PREFIX_CHAR();
  public abstract char TOKEN_POSTFIX_CHAR();
  public abstract int TOKEN_FIXED_CHAR();
  public abstract int TOKEN_NOTE_CHAR();
  public abstract int TOKEN_TAG_CHAR();
  public abstract int TOKEN_EXPR_START_CHAR();
  public abstract int TOKEN_EXPR_END_CHAR();
  public abstract int TOKEN_NEWLINE_CHAR();
  public abstract int TOKEN_TRIM_CHAR();
  
  public int TOKEN_PREFIX() {
    return TOKEN_PREFIX_CHAR();
  }
  public int TOKEN_POSTFIX() {
    return TOKEN_POSTFIX_CHAR();
  }
  public int TOKEN_FIXED() {
    return TOKEN_FIXED_CHAR();
  }
  public int TOKEN_NOTE() {
    return TOKEN_NOTE_CHAR();
  }
  public int TOKEN_TAG() {
    return TOKEN_TAG_CHAR();
  }
  public int TOKEN_EXPR_START() {
    return TOKEN_EXPR_START_CHAR();
  }
  public int TOKEN_EXPR_END() {
    return TOKEN_EXPR_END_CHAR();
  }
  public int TOKEN_NEWLINE() {
    return TOKEN_NEWLINE_CHAR();
  }
  public int TOKEN_TRIM() {
    return TOKEN_TRIM_CHAR();
  }

}
