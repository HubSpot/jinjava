package com.hubspot.jinjava.tree.parse;

public class UnclosedToken extends TextToken {

  public UnclosedToken(
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols
  ) {
    this(image, lineNumber, startPosition, symbols, WhitespaceControlParser.LENIENT);
  }

  public UnclosedToken(
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols,
    WhitespaceControlParser whitespaceControlParser
  ) {
    super(image, lineNumber, startPosition, symbols, whitespaceControlParser);
  }
}
