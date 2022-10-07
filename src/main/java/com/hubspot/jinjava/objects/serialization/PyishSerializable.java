package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Objects;

public interface PyishSerializable extends PyWrapper {
  ObjectWriter SELF_WRITER = new ObjectMapper(
    new JsonFactoryBuilder().quoteChar('\'').build()
  )
    .writer(PyishPrettyPrinter.INSTANCE)
    .with(PyishCharacterEscapes.INSTANCE);
  /**
   * Allows for a class to specify a custom string representation in Jinjava.
   * By default, this will get a json representation of the object,
   * but this method can be overridden to provide a custom representation.
   * This should use double quotes to wrap json keys/values.
   * @return A pyish/json string representation of the object
   */
  default String toPyishString() {
    return writeValueAsString(this);
  }

  /**
   * Utility method to assist implementations of PyishSerializable in
   * overriding <code>toPyishString()</code>.
   * @param value Value to write to a string
   * @return Pyish string value in JSON format
   */
  static String writeValueAsString(Object value) {
    try {
      return SELF_WRITER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return '\'' + Objects.toString(value) + '\'';
    }
  }
}
