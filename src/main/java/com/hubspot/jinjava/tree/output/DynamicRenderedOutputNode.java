package com.hubspot.jinjava.tree.output;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;

/**
 * An OutputNode that can be modified after already being added to the OutputList
 */
public class DynamicRenderedOutputNode implements OutputNode {

  protected String output = "";

  public void setValue(String output) {
    this.output = output;
  }

  @Override
  public String getValue() {
    return output;
  }

  @Override
  public long getSize() {
    return output == null
      ? 0
      : output.getBytes(Charset.forName(Charsets.UTF_8.name())).length;
  }

  @Override
  public String toString() {
    return getValue();
  }
}
