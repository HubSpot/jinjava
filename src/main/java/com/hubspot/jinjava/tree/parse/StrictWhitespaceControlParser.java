package com.hubspot.jinjava.tree.parse;

public class StrictWhitespaceControlParser implements WhitespaceControlParser {

  @Override
  public boolean hasLeftTrim(String unwrapped) {
    return unwrapped.charAt(0) == '-';
  }

  @Override
  public String stripLeft(String unwrapped) {
    return unwrapped.substring(1);
  }

  @Override
  public boolean hasRightTrim(String unwrapped) {
    return unwrapped.charAt(unwrapped.length() - 1) == '-';
  }

  @Override
  public String stripRight(String unwrapped) {
    return unwrapped.substring(0, unwrapped.length() - 1);
  }
}
