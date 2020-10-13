package com.hubspot.jinjava.interpret;

public class EagerValueException extends DeferredValueException {
  private String eagerImage;

  public EagerValueException(String message, String eagerImage) {
    super(message);
    this.eagerImage = eagerImage;
  }

  public EagerValueException(
    String variable,
    String eagerImage,
    int lineNumber,
    int startPosition
  ) {
    super(variable, lineNumber, startPosition);
    this.eagerImage = eagerImage;
  }

  public String getEagerImage() {
    return eagerImage;
  }
}
