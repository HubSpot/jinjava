package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SizeLimitingJsonProcessingException extends JsonProcessingException {
  private final int maxSize;
  private final int attemptedSize;

  protected SizeLimitingJsonProcessingException(int maxSize, int attemptedSize) {
    super(
      String.format(
        "Max length of %d chars reached when serializing. %d chars attempted.",
        maxSize,
        attemptedSize
      )
    );
    this.maxSize = maxSize;
    this.attemptedSize = attemptedSize;
  }

  public int getAttemptedSize() {
    return attemptedSize;
  }

  public int getMaxSize() {
    return maxSize;
  }
}
