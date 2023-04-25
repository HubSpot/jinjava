package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.IOException;
import java.util.Objects;

@Beta
public class PyishSerializer extends JsonSerializer<Object> {
  public static final PyishSerializer INSTANCE = new PyishSerializer();

  private PyishSerializer() {}

  public void serialize(
    Object object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    jsonGenerator.setPrettyPrinter(PyishPrettyPrinter.INSTANCE);
    jsonGenerator.setCharacterEscapes(PyishCharacterEscapes.INSTANCE);
    String string;
    Object wrappedObject = JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.wrap(object))
      .orElse(object);
    if (wrappedObject instanceof PyishSerializable) {
      ((PyishSerializable) wrappedObject).writePyishSelf(
          jsonGenerator,
          serializerProvider
        );
    } else if (wrappedObject instanceof Boolean) {
      jsonGenerator.writeBoolean((Boolean) wrappedObject);
    } else if (wrappedObject instanceof Number) {
      jsonGenerator.writeNumber(wrappedObject.toString());
    } else if (wrappedObject instanceof String) {
      jsonGenerator.writeString((String) wrappedObject);
    } else {
      string = Objects.toString(wrappedObject, "");
      try {
        double number = Double.parseDouble(string);
        if (
          string.equals(String.valueOf(number)) ||
          string.equals(String.valueOf((long) number))
        ) {
          jsonGenerator.writeNumber(string);
        } else {
          jsonGenerator.writeString(string);
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
}
