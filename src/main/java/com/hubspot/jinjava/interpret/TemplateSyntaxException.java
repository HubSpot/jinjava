package com.hubspot.jinjava.interpret;

public class TemplateSyntaxException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final String code;

  public TemplateSyntaxException(String code, String message, int lineNumber, int startPosition) {
    super("Syntax error in '" + code + "': " + message, lineNumber, startPosition);
    this.code = code;
  }

  public TemplateSyntaxException(String code, String message, int lineNumber) {
    this(code, message, lineNumber, -1);
  }

  public TemplateSyntaxException(String code, String message, int lineNumber, int startPosition, Throwable t) {
    super(message, t, lineNumber, startPosition);
    this.code = code;
  }

  public TemplateSyntaxException(String code, String message, int lineNumber, Throwable t) {
    this(code, message, lineNumber, -1, t);
  }

  public String getCode() {
    return code;
  }

}
