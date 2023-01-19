package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.atomic.AtomicInteger;

public class DepthAndWidthLimitingException extends JsonProcessingException {

  protected DepthAndWidthLimitingException(AtomicInteger depth) {
    super(
      String.format(
        "Maximum %s reached while serializing",
        depth.get() <= 0 ? "depth" : "width"
      )
    );
  }
}
