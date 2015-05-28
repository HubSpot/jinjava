package com.hubspot.jinjava.interpret;

public class TemplateSyntaxException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final String code;

  public TemplateSyntaxException(String code, String message, int lineNumber) {
    super("Syntax error in '" + code + "': " + message, lineNumber);
    this.code = code;
  }

  public TemplateSyntaxException(String code, String message, int lineNumber, Throwable t) {
    super(message, t, lineNumber);
    this.code = code;
  }

  public String getCode() {
    return code;
  }

}
