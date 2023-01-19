package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SizeLimitingJsonProcessingException extends JsonProcessingException {

  protected SizeLimitingJsonProcessingException(int maxSize, int attemptedSize) {
    super("Max length of {} chars reached when serializing. {} chars attempted.");
  }
}
