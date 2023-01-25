package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

@Beta
public class CannotReconstructValueException extends DeferredValueException {
  public static final String CANNOT_RECONSTRUCT_MESSAGE = "Cannot reconstruct value";

  public CannotReconstructValueException(String key) {
    super(String.format("%s: %s", CANNOT_RECONSTRUCT_MESSAGE, key));
  }
}
