package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;

public interface NodePreProcessor {
  void preProcess(Node node, JinjavaInterpreter interpreter);
}
