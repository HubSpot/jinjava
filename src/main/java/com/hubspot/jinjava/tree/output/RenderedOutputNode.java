package com.hubspot.jinjava.tree.output;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

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

  @Override
  public long getSize() {
    return output == null ? 0 : output.getBytes(Charset.forName(Charsets.UTF_8.name())).length;
  }
}
