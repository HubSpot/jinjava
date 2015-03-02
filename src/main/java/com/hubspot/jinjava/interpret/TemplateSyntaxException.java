package com.hubspot.jinjava.interpret;

public class TemplateSyntaxException extends InterpretException {
  private static final long serialVersionUID = 1L;

  public TemplateSyntaxException(String code, String message, int lineNumber) {
    super("Syntax error in '" + code + "': " + message, lineNumber);
  }

  public TemplateSyntaxException(String code, String message, int lineNumber, Throwable t) {
    super("Syntax error in '" + code + "': " + message, t, lineNumber);
  }
  
}
