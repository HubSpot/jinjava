package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import java.util.function.BiConsumer;

public class JinjavaNodePreProcessor implements BiConsumer<Node, JinjavaInterpreter> {

  @Override
  public void accept(Node node, JinjavaInterpreter interpreter) {
    interpreter.getContext().setCurrentNode(node);
    checkForInterrupt(node);
  }

  protected void checkForInterrupt(Node node) {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterpretException(
        "Interrupt rendering " + getClass(),
        node.getMaster().getLineNumber(),
        node.getMaster().getStartPosition()
      );
    }
  }
}
