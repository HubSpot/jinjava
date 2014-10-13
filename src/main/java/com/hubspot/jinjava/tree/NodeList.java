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
import java.util.Iterator;

public class NodeList implements Iterable<Node>, Serializable, Cloneable {

  private static final long serialVersionUID = -8672835620582591304L;

  Node head = null;
  Node tail = null;
  transient int size = 0;

  public NodeList() {}
  
  public NodeList(Node... nodes) {
    for(Node n : nodes) {
      add(n);
    }
  }
  
  /**
   * trusty call by Node
   * 
   * @param node
   */
  void add(Node node) {
    if (head == null) {
      head = tail = node;
    } else {
      tail.setSuccessor(node);
      node.setPredecessor(tail);
      tail = node;
    }
    size++;
  }

  boolean remove(Node e) {
    if (e == null || head == null || e.getParent() != head.getParent()) {
      return false;
    }
    if (e == head) {
      head = head.getSuccessor();
      head.setPredecessor(null);
    } else if (e == tail) {
      tail = tail.getPredecessor();
      tail.setSuccessor(null);
    } else {
      e.getPredecessor().setSuccessor(e.getSuccessor());
      e.getSuccessor().setPredecessor(e.getPredecessor());
    }
    size--;
    return true;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public Iterator<Node> iterator() {
    return new NodeItr();
  }

  public int size() {
    return size;
  }

  public Node getFirst() {
    return head;
  }

  public Node getLast() {
    return tail;
  }

  @Override
  public NodeList clone() {
    NodeList clone = new NodeList();
    for (Node node : this) {
      Node temp = node.clone();
      temp.setParent(head.getParent());
      clone.add(temp);
    }
    return clone;
  }

  NodeList clone(Node parent) {
    NodeList clone = new NodeList();
    for (Node node : this) {
      Node temp = node.clone();
      temp.setParent(parent);
      clone.add(temp);
    }
    return clone;
  }

  private class NodeItr implements Iterator<Node> {

    Node cursor;

    NodeItr() {
      cursor = head;
    }

    @Override
    public boolean hasNext() {
      return cursor != null;
    }

    @Override
    public Node next() {
      Node temp = cursor;
      if (cursor != tail) {
        cursor = cursor.getSuccessor();
      } else {
        cursor = null;
      }
      return temp;
    }

    @Override
    public void remove() {
      NodeList.this.remove(cursor);
    }

  }

}
