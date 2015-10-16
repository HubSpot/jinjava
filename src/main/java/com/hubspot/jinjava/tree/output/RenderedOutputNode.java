package com.hubspot.jinjava.tree.output;

public class RenderedOutputNode implements OutputNode {

  private final String output;

  public RenderedOutputNode(String output) {
    this.output = output;
  }

  @Override
  public String getValue() {
    return output;
  }

  @Override
  public String toString() {
    return getValue();
  }

}
