package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Objects;

public interface PyishSerializable extends PyWrapper {
  ObjectWriter PYISH_OBJECT_WRITER = new ObjectMapper()
    .registerModule(
      new SimpleModule()
        .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
        .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
    )
    .writer(PyishPrettyPrinter.INSTANCE);
  /**
   * Allows for a class to specify a custom string representation in Jinjava.
   * By default, this will refer to the <code>toString()</code> method,
   * but this method can be overriden to provide a representation separate from the
   * normal <code>toString()</code> result.
   * This should use double quotes to wrap json keys/values.
   * @return A pythonic/json string representation of the object
   */
  default String toPyishString() {
    return '"' + writeValueAsString(this) + '"';
  }

  /**
   * Utility method to assist implementations of PyishSerializable in
   * overriding <code>toPyishString()</code>.
   * @param value Value to write to a string
   * @return Pyish string value in JSON format
   */
  static String writeValueAsString(Object value) {
    try {
      return PYISH_OBJECT_WRITER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return Objects.toString(value);
    }
  }
}
