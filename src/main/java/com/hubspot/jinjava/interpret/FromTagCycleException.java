package com.hubspot.jinjava.interpret;

public class FromTagCycleException extends TagCycleException {
  private static final long serialVersionUID = -5487642459443650227L;

  public FromTagCycleException(String path, int lineNumber, int startPosition) {
    super("From", path, lineNumber, startPosition);
  }

}
