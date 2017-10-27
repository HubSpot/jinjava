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

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.util.WhitespaceUtils;

public class ExpressionToken extends Token {

  private static final long serialVersionUID = 6336768632140743908L;

  private String expr;

  public ExpressionToken(String image, int lineNumber, int startPosition) {
    super(image, lineNumber, startPosition);
  }

  @Override
  public String toString() {
    return "{{ " + getExpr() + "}}";
  }

  @Override
  public int getType() {
    return TOKEN_EXPR_START;
  }

  @Override
  protected void parse() {
    this.expr = WhitespaceUtils.unwrap(image, "{{", "}}");

    if (WhitespaceUtils.startsWith(expr, "-")) {
      setLeftTrim(true);
      this.expr = WhitespaceUtils.unwrap(expr, "-", "");
    }
    if (WhitespaceUtils.endsWith(expr, "-")) {
      setRightTrim(true);
      this.expr = WhitespaceUtils.unwrap(expr, "", "-");
    }

    this.expr = StringUtils.trimToEmpty(this.expr);
  }

  public String getExpr() {
    return expr;
  }

}
