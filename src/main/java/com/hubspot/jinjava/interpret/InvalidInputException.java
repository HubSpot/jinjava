package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.lib.Importable;

public class InvalidInputException extends RuntimeException {

  private final int lineNumber;
  private final int startPosition;
  private final String message;
  private final String name;

  public InvalidInputException(JinjavaInterpreter interpreter, Importable importable, InvalidReason invalidReason, Object... errorMessageArgs) {
    this(interpreter, importable.getName(), String.format("Invalid input for '%s': input %s",
        importable.getName(),
        String.format(invalidReason.getErrorMessage(), errorMessageArgs)));
  }

  public InvalidInputException(JinjavaInterpreter interpreter, String name, String errorMessage) {
    this.message = errorMessage;

    this.lineNumber = interpreter.getLineNumber();
    this.startPosition = interpreter.getPosition();
    this.name = name;
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

  public String getName() {
    return name;
  }
}
