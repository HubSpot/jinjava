package com.hubspot.jinjava.interpret;

public class InvalidArgumentException extends RuntimeException {

  private final int lineNumber;
  private final int startPosition;
  private final String message;
  private final String fieldName;

  public InvalidArgumentException(JinjavaInterpreter interpreter, String code, String message, String fieldName) {
    this.fieldName = fieldName;
    this.message = String.format("Invalid argument in %s: %s", code, message);
    this.lineNumber = interpreter.getLineNumber();
    this.startPosition = interpreter.getPosition();
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getMessage() {
    return message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getStartPosition() {
    return startPosition;
  }
}
