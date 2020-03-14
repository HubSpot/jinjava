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
  public char getTokenPrefixChar() {
    return TOKEN_PREFIX_CHAR;
  }
  @Override
  public char getTokenPostfixChar() {
    return TOKEN_POSTFIX_CHAR;
  }
  @Override
  public char getTokenFixedChar() {
    return TOKEN_FIXED_CHAR;
  }
  @Override
  public char getTokenNoteChar() {
    return TOKEN_NOTE_CHAR;
  }
  @Override
  public char getTokenTagChar() {
    return TOKEN_TAG_CHAR;
  }
  @Override
  public char getTokenExprStartChar() {
    return TOKEN_EXPR_START_CHAR;
  }
  @Override
  public char getTokenExprEndChar() {
    return TOKEN_EXPR_END_CHAR;
  }
  @Override
  public char getTokenNewlineChar() {
    return TOKEN_NEWLINE_CHAR;
  }
  @Override
  public char getTokenTrimChar() {
    return TOKEN_TRIM_CHAR;
  }

}
