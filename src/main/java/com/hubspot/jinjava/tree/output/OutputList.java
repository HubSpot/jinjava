package com.hubspot.jinjava.tree.output;

import java.util.LinkedList;
import java.util.List;

public class OutputList {

  private final List<OutputNode> nodes = new LinkedList<>();
  private final List<BlockPlaceholderOutputNode> blocks = new LinkedList<>();

  public void addNode(OutputNode node) {
    nodes.add(node);

    if (node instanceof BlockPlaceholderOutputNode) {
      blocks.add((BlockPlaceholderOutputNode) node);
    }
  }

  public List<BlockPlaceholderOutputNode> getBlocks() {
    return blocks;
  }

  public String getValue() {
    StringBuilder val = new StringBuilder();

    for (OutputNode node : nodes) {
      val.append(node.getValue());
    }

    return val.toString();
  }

  @Override
  public String toString() {
    return getValue();
  }

}
