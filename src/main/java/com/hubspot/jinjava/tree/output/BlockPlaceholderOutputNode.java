package com.hubspot.jinjava.tree.output;

public class BlockPlaceholderOutputNode implements OutputNode {

  private final String blockName;
  private String output;

  public BlockPlaceholderOutputNode(String blockName) {
    this.blockName = blockName;
  }

  public String getBlockName() {
    return blockName;
  }

  public boolean isResolved() {
    return output != null;
  }

  public void resolve(String output) {
    this.output = output;
  }

  @Override
  public String getValue() {
    if (output == null) {
      throw new IllegalStateException("Block placeholder not resolved: " + blockName);
    }

    return output;
  }

  @Override
  public String toString() {
    return getValue();
  }

}
