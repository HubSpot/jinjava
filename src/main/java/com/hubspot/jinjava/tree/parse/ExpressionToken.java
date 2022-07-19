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

import com.hubspot.jinjava.util.WhitespaceUtils;
import org.apache.commons.lang3.StringUtils;

public class ExpressionToken extends Token {
  private static final long serialVersionUID = 6336768632140743908L;
  private String expr;

  public ExpressionToken(
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols
  ) {
    super(image, lineNumber, startPosition, symbols);
  }

  @Override
  public String toString() {
    return "{{ " + getExpr() + "}}";
  }

  @Override
  public int getType() {
    return getSymbols().getExprStart();
  }

  @Override
  protected void parse() {
    this.expr = WhitespaceUtils.unwrap(image, "{{", "}}");

    if (expr.charAt(0) == '-') {
      setLeftTrim(true);
      this.expr = expr.substring(1);
    }
    if (expr.charAt(expr.length() - 1) == '-') {
      setRightTrim(true);
      this.expr = expr.substring(0, expr.length() - 1);
    }

    this.expr = StringUtils.trimToEmpty(this.expr);
  }

  public String getExpr() {
    return expr;
  }
}
