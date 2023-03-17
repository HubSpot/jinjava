package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hubspot.jinjava.lib.filter.AllowSnakeCaseFilter;
import java.io.IOException;

public class BothCasingBeanSerializer<T> extends JsonSerializer<T> {
  private final JsonSerializer<T> orignalSerializer;

  private BothCasingBeanSerializer(JsonSerializer<T> jsonSerializer) {
    this.orignalSerializer = jsonSerializer;
  }

  public static <T> BothCasingBeanSerializer<T> wrapping(
    JsonSerializer<T> jsonSerializer
  ) {
    return new BothCasingBeanSerializer<>(jsonSerializer);
  }

  @Override
  public void serialize(
    T value,
    JsonGenerator gen,
    SerializerProvider serializerProvider
  )
    throws IOException {
    if (
      Boolean.TRUE.equals(
        serializerProvider.getAttribute(PyishObjectMapper.ALLOW_SNAKE_CASE_ATTRIBUTE)
      )
    ) {
      // if it's directly for output, then we don't want to add the additional filter characters,
      // as doing so would make the "|allow_snake_case" appear in the final output.
      StringBuilder sb = new StringBuilder();
      sb
        .append(PyishSerializable.writeValueAsString(value))
        .append('|')
        .append(AllowSnakeCaseFilter.NAME);
      gen.writeRawValue(sb.toString());
    } else {
      orignalSerializer.serialize(value, gen, serializerProvider);
    }
  }
}
