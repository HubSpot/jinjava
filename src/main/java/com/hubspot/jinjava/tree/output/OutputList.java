package com.hubspot.jinjava.tree.output;

import java.util.LinkedList;
import java.util.List;

import com.hubspot.jinjava.interpret.OutputTooBigException;

public class OutputList {

  private final List<OutputNode> nodes = new LinkedList<>();
  private final List<BlockPlaceholderOutputNode> blocks = new LinkedList<>();
  private final long maxOutputSize;
  private long currentSize;

  public OutputList(long maxOutputSize) {
    this.maxOutputSize = maxOutputSize;
  }

  public void addNode(OutputNode node) {

    if (maxOutputSize > 0 && currentSize + node.getSize() > maxOutputSize) {
      throw new OutputTooBigException(maxOutputSize, currentSize + node.getSize());
    }

    currentSize += node.getSize();
    nodes.add(node);

    if (node instanceof BlockPlaceholderOutputNode) {
      BlockPlaceholderOutputNode blockNode = (BlockPlaceholderOutputNode) node;

      if (maxOutputSize > 0 && currentSize + blockNode.getSize() > maxOutputSize) {
        throw new OutputTooBigException(maxOutputSize, currentSize + blockNode.getSize());
      }

      currentSize += blockNode.getSize();
      blocks.add(blockNode);
    }
  }

  public List<BlockPlaceholderOutputNode> getBlocks() {
    return blocks;
  }

  public String getValue() {
    StringBuilder val = new StringBuilder();

    long valueSize = 0;

    for (OutputNode node : nodes) {
      if (maxOutputSize > 0 && valueSize + node.getSize() > maxOutputSize) {
        throw new OutputTooBigException(maxOutputSize, valueSize + node.getSize());
      }
      valueSize += node.getSize();
      val.append(node.getValue());
    }

    return val.toString();
  }

  @Override
  public String toString() {
    return getValue();
  }

}
