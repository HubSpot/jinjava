package com.hubspot.jinjava.interpret;

public class ExtendsTagCycleException extends InterpretException {
  private static final long serialVersionUID = -3058494056577268723L;

  private final String path;

  public ExtendsTagCycleException(String path, int lineNumber) {
    super("Extends tag cycle for path '" + path + "'", lineNumber);
    this.path = path;
  }

  public String getPath() {
    return path;
  }

}
