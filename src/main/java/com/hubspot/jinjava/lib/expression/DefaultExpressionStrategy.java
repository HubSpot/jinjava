package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.EscapeFilter;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.util.Logging;

public class DefaultExpressionStrategy implements ExpressionStrategy {
  private static final long serialVersionUID = 436239440273704843L;

  public RenderedOutputNode interpretOutput(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    Object var = interpreter.resolveELExpression(
      master.getExpr(),
      master.getLineNumber()
    );
    String result = interpreter.getAsString(var);

    if (shouldDoNestedInterpretation(result, master, interpreter)) {
      try {
        result = interpreter.renderFlat(result);
      } catch (Exception e) {
        Logging.ENGINE_LOG.warn("Error rendering variable node result", e);
      }
    }

    if (interpreter.getContext().isAutoEscape() && !(var instanceof SafeString)) {
      result = EscapeFilter.escapeHtmlEntities(result);
    }

    return new RenderedOutputNode(result);
  }
}
