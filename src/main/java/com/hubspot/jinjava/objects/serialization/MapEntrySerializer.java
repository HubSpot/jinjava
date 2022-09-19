package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;

public class MapEntrySerializer extends JsonSerializer<Map.Entry> {
  public static final MapEntrySerializer INSTANCE = new MapEntrySerializer();

  private MapEntrySerializer() {}

  @Override
  public void serialize(
    Map.Entry object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    String key = PyishObjectMapper.PYISH_OBJECT_WRITER.writeValueAsString(
      object.getKey()
    );
    String value = PyishObjectMapper.PYISH_OBJECT_WRITER.writeValueAsString(
      object.getValue()
    );
    jsonGenerator.writeRawValue(String.format("fn:map_entry(%s, %s)", key, value));
  }
}
