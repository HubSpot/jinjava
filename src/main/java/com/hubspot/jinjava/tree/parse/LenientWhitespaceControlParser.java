package com.hubspot.jinjava.tree.parse;

import com.hubspot.jinjava.util.WhitespaceUtils;

public class LenientWhitespaceControlParser implements WhitespaceControlParser {

  @Override
  public boolean hasLeftTrim(String unwrapped) {
    return WhitespaceUtils.startsWith(unwrapped, "-");
  }

  @Override
  public String stripLeft(String unwrapped) {
    return WhitespaceUtils.unwrap(unwrapped, "-", "");
  }

  @Override
  public boolean hasRightTrim(String unwrapped) {
    return WhitespaceUtils.endsWith(unwrapped, "-");
  }

  @Override
  public String stripRight(String unwrapped) {
    return WhitespaceUtils.unwrap(unwrapped, "", "-");
  }
}
