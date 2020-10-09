package com.hubspot.jinjava.interpret;

public class PreservedRawTagException extends InterpretException {

  public PreservedRawTagException() {
    super("Encountered a preserved raw tag");
  }

  public PreservedRawTagException(int lineNumber, int startPosition) {
    super("Encountered a preserved raw tag", lineNumber, startPosition);
  }
}
