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
package com.hubspot.jinjava.tree;

import java.io.Serializable;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.parse.Token;

public abstract class Node implements Serializable, Cloneable {

  private static final long serialVersionUID = 7323842986596895498L;

  private int level = 0;
  private int lineNumber = 0;
  private Node parent = null;
  private Node predecessor = null;
  private Node successor = null;
  private NodeList children = new NodeList();
  private Token master;

  public Node(Token master, int lineNumber) {
    this.master = master;
    this.lineNumber = lineNumber;
  }

  public int getLevel() {
    return level;
  }
  
  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }
  
  public Node getPredecessor() {
    return predecessor;
  }

  public void setPredecessor(Node predecessor) {
    this.predecessor = predecessor;
  }
  
  public Node getSuccessor() {
    return successor;
  }

  public void setSuccessor(Node successor) {
    this.successor = successor;
  }
  
  public Token getMaster() {
    return master;
  }
  
  public int getLineNumber() {
    return lineNumber;
  }

  public NodeList getChildren() {
    return children;
  }
  
  public void setChildren(NodeList children) {
    this.children = children;
  }

  @Override
  public abstract Node clone();

  /**
   * trusty call by TreeParser
   * 
   * @param node
   */
  void add(Node node) {
    node.level = level + 1;
    node.parent = this;
    children.add(node);
  }

  Node treeNext() {
    if (children.size > 0) {
      return children.head;
    } else {
      return recursiveNext();
    }
  }

  Node recursiveNext() {
    if (successor != null) {
      return successor;
    } else {
      if (parent != null) {
        return parent.recursiveNext();
      } else {
        return null;
      }
    }
  }

  void computeLevel(int baseLevel) {
    level = baseLevel;
    for (Node child : children) {
      child.computeLevel(level + 1);
    }
  }

  public abstract String render(JinjavaInterpreter interpreter);

  public abstract String getName();

}
