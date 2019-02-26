package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.lib.Importable;

public class InvalidArgumentException extends RuntimeException {

  private final int lineNumber;
  private final int startPosition;
  private final String message;

  public InvalidArgumentException(JinjavaInterpreter interpreter, Importable importable, InvalidReason invalidReason, int argumentNumber, Object... errorMessageArgs) {
    this.message = String.format("Invalid argument in '%s': %s",
        importable.getName(),
        String.format("%s %s", formatArgumentNumber(argumentNumber + 1), String.format(invalidReason.getErrorMessage(), errorMessageArgs)));

    this.lineNumber = interpreter.getLineNumber();
    this.startPosition = interpreter.getPosition();
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

  private static String formatArgumentNumber(int argumentNumber) {
    switch (argumentNumber){
      case 1:
        return "1st";
      case 2:
        return "2nd";
      case 3:
        return "3rd";
      case 4:
        return "4th";
      case 5:
        return "5th";
      case 6:
        return "6th";
      case 7:
        return "7th";
      case 8:
        return "8th";
      case 9:
        return "9th";
      default:
        return String.valueOf(argumentNumber);

    }
  }
}
