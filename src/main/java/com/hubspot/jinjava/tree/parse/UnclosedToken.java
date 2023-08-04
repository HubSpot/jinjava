package com.hubspot.jinjava.tree.parse;

public class UnclosedToken extends TextToken {

  public UnclosedToken(
    String image,
    int lineNumber,
    int startPosition,
    TokenScannerSymbols symbols
  ) {
    super(image, lineNumber, startPosition, symbols);
  }
}
