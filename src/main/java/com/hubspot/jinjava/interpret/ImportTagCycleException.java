package com.hubspot.jinjava.interpret;

public class ImportTagCycleException extends TagCycleException {
  private static final long serialVersionUID = 1092085697026161185L;

  public ImportTagCycleException(String path, int lineNumber, int startPosition) {
    super("Import", path, lineNumber, startPosition);
  }

}
