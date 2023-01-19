package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hubspot.jinjava.objects.PyWrapper;
import java.io.IOException;
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
   * This method will be used by {@link #writeSelf(JsonGenerator, SerializerProvider)}
   * to specify what will be written to the json generator.
   * @return A pyish/json CharSequence representation of the object
   */
  default CharSequence toPyishString() {
    return writeValueAsString(this);
  }

  /**
   * Allows for a class to specify how its pyish string representation will
   * be written to the json generator.
   * If the pyish string representation of this object can be very large, it's recommended
   * to override this method instead of {@link #toPyishString()} so that jsonGenerator
   * can be written to multiple times, allowing multiple limit checks to occur.
   */
  default void writeSelf(
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    jsonGenerator.writeRawValue(toPyishString().toString());
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
