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

public interface TokenScannerSymbols {

  char TOKEN_PREFIX_CHAR = '{';
  char TOKEN_POSTFIX_CHAR = '}';
  char TOKEN_FIXED_CHAR = 0;
  char TOKEN_NOTE_CHAR = '#';
  char TOKEN_TAG_CHAR = '%';
  char TOKEN_EXPR_START_CHAR = '{';
  char TOKEN_EXPR_END_CHAR = '}';
  char TOKEN_NEWLINE_CHAR = '\n';

  int TOKEN_PREFIX = TOKEN_PREFIX_CHAR;
  int TOKEN_POSTFIX = TOKEN_POSTFIX_CHAR;
  int TOKEN_FIXED = TOKEN_FIXED_CHAR;
  int TOKEN_NOTE = TOKEN_NOTE_CHAR;
  int TOKEN_TAG = TOKEN_TAG_CHAR;
  int TOKEN_EXPR_START = TOKEN_EXPR_START_CHAR;
  int TOKEN_EXPR_END = TOKEN_EXPR_END_CHAR;
  int TOKEN_NEWLINE = TOKEN_NEWLINE_CHAR;

}
