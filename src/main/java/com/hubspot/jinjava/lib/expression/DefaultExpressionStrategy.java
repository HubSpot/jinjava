package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.EscapeFilter;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.util.Logging;
import org.apache.commons.lang3.StringUtils;

public class DefaultExpressionStrategy implements ExpressionStrategy {

  public RenderedOutputNode interpretOutput(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    Object var = interpreter.resolveELExpression(
      master.getExpr(),
      master.getLineNumber()
    );
    String result = interpreter.getAsString(var);

    if (interpreter.getConfig().isNestedInterpretationEnabled()) {
      if (
        !StringUtils.equals(result, master.getImage()) &&
        (
          StringUtils.contains(result, master.getSymbols().getExpressionStart()) ||
          StringUtils.contains(result, master.getSymbols().getExpressionStartWithTag())
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
}
