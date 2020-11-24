package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;

public class EagerExpressionStrategy implements ExpressionStrategy {

  @Override
  public RenderedOutputNode interpretOutput(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    return new DefaultExpressionStrategy().interpretOutput(master, interpreter); // TODO replace with actual functionality
  }
}
