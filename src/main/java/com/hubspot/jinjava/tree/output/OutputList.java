package com.hubspot.jinjava.tree.output;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.LinkedList;
import java.util.List;

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

  public List<OutputNode> getNodes() {
    return nodes;
  }

  public List<BlockPlaceholderOutputNode> getBlocks() {
    return blocks;
  }

  public String getValue() {
    LengthLimitingStringBuilder val = new LengthLimitingStringBuilder(maxOutputSize);

    for (OutputNode node : nodes) {
      try {
        val.append(node.getValue());
      } catch (OutputTooBigException e) {
        JinjavaInterpreter
          .getCurrent()
          .addError(TemplateError.fromOutputTooBigException(e));
        return val.toString();
      }
    }

    return val.toString();
  }

  @Override
  public String toString() {
    return getValue();
  }
}
