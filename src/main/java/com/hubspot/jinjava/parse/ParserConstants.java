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
package com.hubspot.jinjava.parse;

public interface ParserConstants {

  int TOKEN_PREFIX = '{';
  int TOKEN_POSTFIX = '}';
  int TOKEN_FIXED = 0;
  int TOKEN_NOTE = '#';
  int TOKEN_TAG = '%';
  int TOKEN_ECHO = '{';
  int TOKEN_ECHO2 = '}';
  int TOKEN_NEWLINE = '\n';
  int TOKEN_SQUOT = '\'';
  int TOKEN_DQUOT = '"';

  char VL = '|';
  char CL = ':';
  char CM = ',';
  char SQ = '\'';
  char DQ = '"';
  char SP = ' ';

}
