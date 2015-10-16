package com.hubspot.jinjava.interpret;

public class TagCycleException extends InterpretException {
  private static final long serialVersionUID = -3058494056577268723L;

  private final String path;
  private final String tagName;

  public TagCycleException(String tagName, String path, int lineNumber) {
    super(tagName + " tag cycle for path '" + path + "'", lineNumber);

    this.path = path;
    this.tagName = tagName;
  }

  public String getPath() {
    return path;
  }

  public String getTagName() {
    return tagName;
  }

}
