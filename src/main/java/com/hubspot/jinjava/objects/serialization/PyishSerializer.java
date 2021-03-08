package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;

public class PyishSerializer extends JsonSerializer<Object> {
  public static final PyishSerializer INSTANCE = new PyishSerializer();

  private PyishSerializer() {}

  @Override
  public void serialize(
    Object object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    jsonGenerator.setPrettyPrinter(PyishPrettyPrinter.INSTANCE);
    jsonGenerator.setCharacterEscapes(PyishCharacterEscapes.INSTANCE);
    String string;
    if (object instanceof PyishSerializable) {
      string = ((PyishSerializable) object).toPyishString();
    } else {
      string = Objects.toString(object, "");
    }
    try {
      Double.parseDouble(string);
      if (
        string.length() > 1 &&
        ((string.charAt(0) == '0' && string.indexOf('.') != 1) || string.charAt(0) == '+')
      ) {
        jsonGenerator.writeString(string);
      } else {
        jsonGenerator.writeNumber(string);
      }
    } catch (NumberFormatException e) {
      if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
        jsonGenerator.writeBoolean(Boolean.parseBoolean(string));
      } else {
        jsonGenerator.writeString(string);
      }
    }
  }
}
