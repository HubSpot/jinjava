package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.lib.Importable;

public class InvalidInputException extends RuntimeException {

  private final int lineNumber;
  private final int startPosition;
  private final String message;
  private final String name;

  public InvalidInputException(JinjavaInterpreter interpreter, Importable importable, InvalidReason invalidReason, Object... errorMessageArgs) {
    this(interpreter, importable, String.format("Invalid input in '%s': input variable %s",
        importable.getName(),
        String.format(invalidReason.getErrorMessage(), errorMessageArgs)));
  }

  public InvalidInputException(JinjavaInterpreter interpreter, Importable importable, String errorMessage) {
    this.message = errorMessage;

    this.lineNumber = interpreter.getLineNumber();
    this.startPosition = interpreter.getPosition();
    this.name = importable.getName();
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
