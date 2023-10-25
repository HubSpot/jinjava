package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import java.util.function.BiConsumer;

public class JinjavaNodeProcessor implements BiConsumer<Node, JinjavaInterpreter> {

  @Override
  public void accept(Node node, JinjavaInterpreter interpreter) {}
}
