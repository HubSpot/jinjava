package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DepthAndWidthLimitingException extends JsonProcessingException {

  protected DepthAndWidthLimitingException(DepthAndWidthTracker tracker) {
    super(
      String.format(
        "Maximum %s reached while serializing",
        tracker.getRemainingDepth() < 0 ? "depth" : "width"
      )
    );
  }
}
