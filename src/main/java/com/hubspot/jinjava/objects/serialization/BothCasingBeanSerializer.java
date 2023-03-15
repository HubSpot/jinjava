package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hubspot.jinjava.lib.filter.AllowSnakeCaseFilter;
import java.io.IOException;

public class BothCasingBeanSerializer extends JsonSerializer<Object> {
  public static final BothCasingBeanSerializer INSTANCE = new BothCasingBeanSerializer();

  private BothCasingBeanSerializer() {}

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    StringBuilder sb = new StringBuilder();
    sb
      .append(PyishSerializable.writeValueAsString(value))
      .append('|')
      .append(AllowSnakeCaseFilter.NAME);
    gen.writeRawValue(sb.toString());
  }
}
