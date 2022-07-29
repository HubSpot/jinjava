package com.hubspot.jinjava.tree.parse;

public interface WhitespaceControlParser {
  WhitespaceControlParser LENIENT = new LenientWhitespaceControlParser();
  WhitespaceControlParser STRICT = new StrictWhitespaceControlParser();

  boolean hasLeftTrim(String unwrapped);
  String stripLeft(String unwrapped);

  boolean hasRightTrim(String unwrapped);
  String stripRight(String unwrapped);
}
