package com.hubspot.jinjava.tree.output;

import java.util.LinkedList;
import java.util.List;

import com.hubspot.jinjava.interpret.OutputTooBigException;

public class OutputList {

  private final List<OutputNode> nodes = new LinkedList<>();
  private final List<BlockPlaceholderOutputNode> blocks = new LinkedList<>();
  private long maxOutputSize;

  public OutputList(long maxOutputSize) {
    this.maxOutputSize = maxOutputSize;
  }

  public void addNode(OutputNode node) {

    if (maxOutputSize > 0 && node.getSize() > maxOutputSize) {
      throw new OutputTooBigException(node.getSize());
    }

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
