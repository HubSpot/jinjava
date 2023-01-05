package com.hubspot.jinjava.interpret;

public class CannotReconstructValueException extends DeferredValueException {
  public static final String CANNOT_RECONSTRUCT_MESSAGE = "Cannot reconstruct value";

  public CannotReconstructValueException(String key) {
    super(String.format("%s: %s", CANNOT_RECONSTRUCT_MESSAGE, key));
  }
}
