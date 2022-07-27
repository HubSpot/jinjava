package com.hubspot.jinjava.tree.parse;

public class StrictWhitespaceControlParser implements WhitespaceControlParser {

  @Override
  public boolean hasLeftTrim(String unwrapped) {
    return unwrapped.startsWith("-");
  }

  @Override
  public String stripLeft(String unwrapped) {
    return unwrapped.substring(1);
  }

  @Override
  public boolean hasRightTrim(String unwrapped) {
    return unwrapped.endsWith("-");
  }

  @Override
  public String stripRight(String unwrapped) {
    return unwrapped.substring(0, unwrapped.length() - 1);
  }
}
