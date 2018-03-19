package com.hubspot.jinjava.interpret;

public class ExtendsTagCycleException extends TagCycleException {
  private static final long serialVersionUID = 3183769038400532542L;

  public ExtendsTagCycleException(String path, int lineNumber, int startPosition) {
    super("Extends", path, lineNumber, startPosition);
  }

}
