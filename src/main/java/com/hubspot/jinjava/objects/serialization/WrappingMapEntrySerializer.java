package com.hubspot.jinjava.objects.serialization;

import static com.hubspot.jinjava.objects.serialization.DepthAndWidthLimitingSerializerFactory.DEPTH_KEY;
import static com.hubspot.jinjava.objects.serialization.DepthAndWidthLimitingSerializerFactory.WIDTH_KEY;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map.Entry;

public class WrappingMapEntrySerializer
  extends JsonSerializer<Entry>
  implements DepthAndWidthLimiting<Entry> {
  public static final WrappingMapEntrySerializer INSTANCE = new WrappingMapEntrySerializer();

  private WrappingMapEntrySerializer() {}

  @Override
  public void serialize(
    Entry entry,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    DepthAndWidthLimitingSerializerFactory.checkDepthAndWidth(
      serializerProvider,
      () -> innerSerialize(entry, jsonGenerator, serializerProvider)
    );
  }

  @Override
  public void innerSerialize(
    Entry entry,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    ObjectWriter objectWriter = PyishObjectMapper
      .PYISH_OBJECT_WRITER.withAttribute(
        DEPTH_KEY,
        serializerProvider.getAttribute(DEPTH_KEY)
      )
      .withAttribute(WIDTH_KEY, serializerProvider.getAttribute(WIDTH_KEY));
    String key = objectWriter.writeValueAsString(entry.getKey());
    String value = objectWriter.writeValueAsString(entry.getValue());
    jsonGenerator.writeRawValue(String.format("fn:map_entry(%s, %s)", key, value));
  }
}
