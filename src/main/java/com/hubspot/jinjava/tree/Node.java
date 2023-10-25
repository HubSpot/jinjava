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

import com.hubspot.jinjava.el.JinjavaProcessors;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.io.Serializable;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;

public abstract class Node implements Serializable {
  private static final long serialVersionUID = -6194634312533310816L;

  private final Token master;
  private final int lineNumber;
  private final int startPosition;

  private Node parent = null;
  private LinkedList<Node> children = new LinkedList<>();

  public Node(Token master, int lineNumber, int startPosition) {
    this.master = master;
    this.lineNumber = lineNumber;
    this.startPosition = startPosition;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public Token getMaster() {
    return master;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public LinkedList<Node> getChildren() {
    return children;
  }

  public void setChildren(LinkedList<Node> children) {
    this.children = children;
  }

  public String reconstructImage() {
    return master.getImage();
  }

  public TokenScannerSymbols getSymbols() {
    return master.getSymbols();
  }

  public abstract OutputNode render(JinjavaInterpreter interpreter);

  public abstract String getName();

  public String toTreeString() {
    return toTreeString(0);
  }

  public String toTreeString(int level) {
    String prefix = StringUtils.repeat(" ", level * 4) + " ";
    StringBuilder t = new StringBuilder(prefix).append(toString()).append('\n');

    for (Node n : getChildren()) {
      t.append(n.toTreeString(level + 1));
    }

    if (getChildren().size() > 0) {
      t.append(prefix).append("end :: ").append(this).append('\n');
    }

    return t.toString();
  }

  public void preProcess(JinjavaInterpreter interpreter) {
    JinjavaProcessors processors = interpreter.getConfig().getProcessors();
    if (processors != null && processors.getNodePreProcessor() != null) {
      processors.getNodePreProcessor().accept(this, interpreter);
    }
  }

  public void postProcess(JinjavaInterpreter interpreter) {
    JinjavaProcessors processors = interpreter.getConfig().getProcessors();
    if (processors != null && processors.getNodePostProcessor() != null) {
      processors.getNodePostProcessor().accept(this, interpreter);
    }
  }
}
