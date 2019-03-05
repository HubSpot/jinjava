package com.hubspot.jinjava.interpret;

public class TemplateSyntaxException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final String code;

  @Deprecated
  public TemplateSyntaxException(String code, String message, int lineNumber, int startPosition) {
    super(String.format("Syntax error in '%s': %s", code, message), lineNumber, startPosition);
    this.code = code;
  }

  @Deprecated
  public TemplateSyntaxException(String code, String message, int lineNumber) {
    this(code, message, lineNumber, -1);
  }

  @Deprecated
  public TemplateSyntaxException(String code, String message, int lineNumber, int startPosition, Throwable t) {
    super(message, t, lineNumber, startPosition);
    this.code = code;
  }

  @Deprecated
  public TemplateSyntaxException(String code, String message, int lineNumber, Throwable t) {
    this(code, message, lineNumber, -1, t);
  }

  public TemplateSyntaxException(JinjavaInterpreter interpreter, String code, String message) {
    this(code, message, interpreter.getLineNumber(), interpreter.getPosition());
  }

  public String getCode() {
    return code;
  }

}
