package com.hubspot.jinjava.interpret;

public class MacroTagCycleException extends TagCycleException {

  private static final long serialVersionUID = -7552850581260771832L;

  public MacroTagCycleException(String path, int lineNumber, int startPosition) {
    super("Macro", path, lineNumber, startPosition);
  }

}
