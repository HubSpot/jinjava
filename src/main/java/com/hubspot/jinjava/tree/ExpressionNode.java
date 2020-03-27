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
package com.hubspot.jinjava.tree;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.EscapeFilter;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.Logging;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class ExpressionNode extends Node {
  private static final long serialVersionUID = -6063173739682221042L;

  private final ExpressionToken master;

  public ExpressionNode(ExpressionToken token) {
    super(token, token.getLineNumber(), token.getStartPosition());
    master = token;
  }

  @Override
  public OutputNode render(JinjavaInterpreter interpreter) {
    Object var;
    try {
      var = interpreter.resolveELExpression(master.getExpr(), getLineNumber());
    } catch (DeferredValueException e) {
      interpreter.getContext().addDeferredNode(this);
      var = master.getImage();
    }

    String result = Objects.toString(var, "");

    TokenScannerSymbols symbols = interpreter.getConfig().getTokenScannerSymbols();

    if (interpreter.getConfig().isNestedInterpretationEnabled()) {
      if (
        !StringUtils.equals(result, master.getImage()) &&
        (
          StringUtils.contains(result, symbols.getExpressionStart()) ||
          StringUtils.contains(result, symbols.getExpressionStartWithTag())
        )
      ) {
        try {
          result = interpreter.renderFlat(result);
        } catch (Exception e) {
          Logging.ENGINE_LOG.warn("Error rendering variable node result", e);
        }
      }
    }

    if (interpreter.getContext().isAutoEscape() && !(var instanceof SafeString)) {
      result = EscapeFilter.escapeHtmlEntities(result);
    }

    return new RenderedOutputNode(result);
  }

  @Override
  public String toString() {
    return master.toString();
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
