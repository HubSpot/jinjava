package com.hubspot.jinjava.interpret;

public class Call {

  private final String path;
  private final int lineNumber;
  private final int startPosition;

  public Call(String path, int lineNumber, int startPosition) {
    this.path = path;
    this.lineNumber = lineNumber;
    this.startPosition = startPosition;
  }
}
