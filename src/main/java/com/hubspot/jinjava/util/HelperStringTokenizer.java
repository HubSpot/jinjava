/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.util;

import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

/**
 * Whitespace and comma as separator quote to accept them as normal char
 *
 * @author fangchq
 *
 */
public class HelperStringTokenizer extends AbstractIterator<String> {

  private final char[] value;
  private final int length;

  private int currPost = 0;
  private int tokenStart = 0;
  private char lastChar = ' ';
  private boolean useComma = false;
  private char quoteChar = 0;
  private boolean inQuote = false;

  public HelperStringTokenizer(String s) {
    value = s.toCharArray();
    length = value.length;
  }

  /**
   * use Comma as token split or not true use it; false don't use it.
   *
   * @param onOrOff
   *          flag to indicate whether or not to split on commas
   * @return this instance for method chaining
   */
  public HelperStringTokenizer splitComma(boolean onOrOff) {
    useComma = onOrOff;
    return this;
  }

  @Override
  protected String computeNext() {
    String token;
    while (currPost < length) {
      token = makeToken();
      lastChar = value[currPost - 1];
      if (token != null) {
        return token;
      }
    }

    endOfData();
    return null;
  }

  private String makeToken() {
    char c = value[currPost++];
    if (c == '"' || c == '\'') {
      if (inQuote) {
        if (quoteChar == c) {
          inQuote = false;
        }
      } else {
        inQuote = true;
        quoteChar = c;
      }
    }
    if ((Character.isWhitespace(c) || (useComma && c == ',')) && !inQuote) {
      return newToken();
    }
    if (currPost == length) {
      return getEndToken();
    }
    return null;
  }

  private String getEndToken() {
    return String.copyValueOf(value, tokenStart, currPost - tokenStart);
  }

  private String newToken() {
    int lastStart = tokenStart;
    tokenStart = currPost;
    if (Character.isWhitespace(lastChar) || (useComma && lastChar == ',')) {
      return null;
    }
    // startChar = -1;//change to save quote in helper
    return String.copyValueOf(value, lastStart, currPost - lastStart - 1);
  }

  public List<String> allTokens() {
    return Lists.newArrayList(this);
  }

}
