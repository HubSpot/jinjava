package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.lib.Importable;

public class InvalidArgumentException extends RuntimeException {

  private final int lineNumber;
  private final int startPosition;
  private final String message;
  private final String name;

  public InvalidArgumentException(JinjavaInterpreter interpreter, Importable importable, InvalidReason invalidReason, int argumentNumber, Object... errorMessageArgs) {
    this(interpreter, importable.getName(), String.format("Invalid argument in '%s': %s",
        importable.getName(),
        String.format("%s argument %s", formatArgumentNumber(argumentNumber + 1), String.format(invalidReason.getErrorMessage(), errorMessageArgs))));
  }

  public InvalidArgumentException(JinjavaInterpreter interpreter, Importable importable, InvalidReason invalidReason, String argumentName, Object... errorMessageArgs) {
    this(interpreter, importable.getName(), String.format("Invalid argument in '%s': %s",
        importable.getName(),
        String.format("'%s' argument %s", argumentName, String.format(invalidReason.getErrorMessage(), errorMessageArgs))));
  }

  public InvalidArgumentException(JinjavaInterpreter interpreter, String name, String errorMessage) {
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

  private static String formatArgumentNumber(int argumentNumber) {

    String base = "th";
    int remainder = argumentNumber % 10;
    if (remainder == 1) {
      base = "st";
    } else if (remainder == 2) {
      base = "nd";
    } else if (remainder == 3) {
      base = "rd";
    }

    return String.format("%d%s", argumentNumber, base);
  }
}
