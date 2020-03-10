package com.hubspot.jinjava.tree.parse;

public class DefaultTokenScannerSymbols extends TokenScannerSymbols {

  char TOKEN_PREFIX_CHAR = '{';
  char TOKEN_POSTFIX_CHAR = '}';
  char TOKEN_FIXED_CHAR = 0;
  char TOKEN_NOTE_CHAR = '#';
  char TOKEN_TAG_CHAR = '%';
  char TOKEN_EXPR_START_CHAR = '{';
  char TOKEN_EXPR_END_CHAR = '}';
  char TOKEN_NEWLINE_CHAR = '\n';
  char TOKEN_TRIM_CHAR = '-';
  
  @Override
  public char TOKEN_PREFIX_CHAR() {
    return TOKEN_PREFIX_CHAR;
  }
  @Override
  public char TOKEN_POSTFIX_CHAR() {
    return TOKEN_POSTFIX_CHAR;
  }
  @Override
  public int TOKEN_FIXED_CHAR() {
    return TOKEN_FIXED_CHAR;
  }
  @Override
  public int TOKEN_NOTE_CHAR() {
    return TOKEN_NOTE_CHAR;
  }
  @Override
  public int TOKEN_TAG_CHAR() {
    return TOKEN_TAG_CHAR;
  }
  @Override
  public int TOKEN_EXPR_START_CHAR() {
    return TOKEN_EXPR_START_CHAR;
  }
  @Override
  public int TOKEN_EXPR_END_CHAR() {
    return TOKEN_EXPR_END_CHAR;
  }
  @Override
  public int TOKEN_NEWLINE_CHAR() {
    return TOKEN_NEWLINE_CHAR;
  }
  @Override
  public int TOKEN_TRIM_CHAR() {
    return TOKEN_TRIM_CHAR;
  }

}
