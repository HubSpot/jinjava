package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public interface DepthAndWidthLimiting<T> {
  void innerSerialize(
    T object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException;
}
