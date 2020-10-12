package com.hubspot.jinjava.interpret;

public class PreservedRawTagException extends InterpretException {
  private String preservedImage;

  public PreservedRawTagException() {
    super("Encountered a preserved raw tag");
  }

  public PreservedRawTagException(
    String preservedImage,
    int lineNumber,
    int startPosition
  ) {
    super("Encountered a preserved raw tag", lineNumber, startPosition);
    this.preservedImage = preservedImage;
  }

  public String getPreservedImage() {
    return preservedImage;
  }
}
