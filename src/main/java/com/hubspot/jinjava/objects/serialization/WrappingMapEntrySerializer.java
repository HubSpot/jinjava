package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map.Entry;

public class WrappingMapEntrySerializer
  extends JsonSerializer<Entry<?, ?>>
  implements DepthAndWidthLimiting<Entry<?, ?>> {
  public static final WrappingMapEntrySerializer INSTANCE = new WrappingMapEntrySerializer();

  private WrappingMapEntrySerializer() {}

  @Override
  public void serialize(
    Entry<?, ?> entry,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    DepthAndWidthLimiting.super.serialize(entry, jsonGenerator, serializerProvider);
  }

  @Override
  public void innerSerialize(
    Entry<?, ?> entry,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    ObjectWriter objectWriter = PyishObjectMapper
      .PYISH_OBJECT_WRITER.withAttribute(
        REMAINING_DEPTH_KEY,
        serializerProvider.getAttribute(REMAINING_DEPTH_KEY)
      )
      .withAttribute(
        REMAINING_WIDTH_KEY,
        serializerProvider.getAttribute(REMAINING_WIDTH_KEY)
      );
    String key = objectWriter.writeValueAsString(entry.getKey());
    String value = objectWriter.writeValueAsString(entry.getValue());
    jsonGenerator.writeRawValue(String.format("fn:map_entry(%s, %s)", key, value));
  }
}
