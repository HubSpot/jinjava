package com.hubspot.jinjava.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;

public class PyishSerializer extends JsonSerializer<Object> {

  public PyishSerializer() {}

  @Override
  public void serialize(
    Object object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    jsonGenerator.setPrettyPrinter(PyishPrettyPrinter.INSTANCE);
    String string;
    if (object instanceof PyishSerializable) {
      string = ((PyishSerializable) object).toPyishString();
    } else {
      string = Objects.toString(object, "");
    }
    try {
      Double.parseDouble(string);
      jsonGenerator.writeNumber(string);
    } catch (NumberFormatException e) {
      if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
        jsonGenerator.writeBoolean(Boolean.parseBoolean(string));
      } else {
        jsonGenerator.writeString(string);
      }
    }
  }
}
