package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.Beta;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

@Beta
public class MapEntrySerializer extends JsonSerializer<Entry<?, ?>> {
  public static final MapEntrySerializer INSTANCE = new MapEntrySerializer();

  private MapEntrySerializer() {}

  @Override
  public void serialize(
    Entry<?, ?> entry,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    AtomicInteger remainingLength = (AtomicInteger) serializerProvider.getAttribute(
      LengthLimitingWriter.REMAINING_LENGTH_ATTRIBUTE
    );
    String key;
    String value;
    ObjectWriter objectWriter = PyishObjectMapper.PYISH_OBJECT_WRITER.withAttribute(
      PyishObjectMapper.ALLOW_SNAKE_CASE_ATTRIBUTE,
      serializerProvider.getAttribute(PyishObjectMapper.ALLOW_SNAKE_CASE_ATTRIBUTE)
    );
    if (remainingLength != null) {
      objectWriter =
        objectWriter.withAttribute(
          LengthLimitingWriter.REMAINING_LENGTH_ATTRIBUTE,
          remainingLength
        );
      key = objectWriter.writeValueAsString(entry.getKey());
      LengthLimitingWriter lengthLimitingWriter = new LengthLimitingWriter(
        new CharArrayWriter(),
        remainingLength
      );
      objectWriter.writeValue(lengthLimitingWriter, entry.getValue());
      value = lengthLimitingWriter.toString();
    } else {
      key = objectWriter.writeValueAsString(entry.getKey());
      value = objectWriter.writeValueAsString(entry.getValue());
    }
    jsonGenerator.writeRawValue(String.format("fn:map_entry(%s, %s)", key, value));
  }
}
