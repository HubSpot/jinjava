package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public interface ExpressionStrategy extends Serializable {
  RenderedOutputNode interpretOutput(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  );

  default boolean shouldDoNestedInterpretation(
    String result,
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    return (
      interpreter.getConfig().isNestedInterpretationEnabled() &&
      !StringUtils.equals(result, master.getImage()) &&
      (
        StringUtils.contains(result, master.getSymbols().getExpressionStart()) ||
        StringUtils.contains(result, master.getSymbols().getExpressionStartWithTag())
      )
    );
  }
}
