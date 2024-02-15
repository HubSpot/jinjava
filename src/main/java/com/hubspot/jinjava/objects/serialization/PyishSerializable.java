package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Beta
public interface PyishSerializable extends PyWrapper {
  ObjectWriter SELF_WRITER = new ObjectMapper(
    new JsonFactoryBuilder().quoteChar('\'').build()
  )
    .writer(PyishPrettyPrinter.INSTANCE)
    .with(PyishCharacterEscapes.INSTANCE);

  /**
   * Allows for a class to append the custom string representation in Jinjava.
   * This method will be used by {@link #writePyishSelf(JsonGenerator, SerializerProvider)}
   * to specify what will be written to the json generator.
   * <p>
   * @param appendable Appendable to append the pyish string representation to.
   * @return The same appendable with an appended result
   */
  @SuppressWarnings("unchecked")
  default <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) appendable.append(writeValueAsString(this));
  }

  /**
   * Allows for a class to specify how its pyish string representation will
   * be written to the json generator.
   * <p>
   * If the object's serialization can be broken up into multiple jsonGenerator writes,
   * then this method can be overridden to do so instead of a single call to
   * {@link JsonGenerator#writeRawValue(String)}.
   * @param jsonGenerator The JsonGenerator to write to.
   * @param serializerProvider Provides default value serialization and attributes stored on the ObjectWriter if needed.
   */
  default void writePyishSelf(
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  ) throws IOException {
    AtomicInteger remainingLength = (AtomicInteger) serializerProvider.getAttribute(
      LengthLimitingWriter.REMAINING_LENGTH_ATTRIBUTE
    );
    jsonGenerator.writeRawValue(
      appendPyishString(
        remainingLength == null
          ? new StringBuilder()
          : new LengthLimitingStringBuilder(remainingLength.get())
      )
        .toString()
    );
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
      if (e.getCause() instanceof DeferredValueException) {
        throw (DeferredValueException) e.getCause();
      }
      return '\'' + Objects.toString(value) + '\'';
    }
  }
}
