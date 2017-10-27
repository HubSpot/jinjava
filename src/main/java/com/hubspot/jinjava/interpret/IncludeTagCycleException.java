package com.hubspot.jinjava.interpret;

public class IncludeTagCycleException extends TagCycleException {
  private static final long serialVersionUID = -5487642459443650227L;

  public IncludeTagCycleException(String path, int lineNumber, int startPosition) {
    super("Include", path, lineNumber, startPosition);
  }

}
