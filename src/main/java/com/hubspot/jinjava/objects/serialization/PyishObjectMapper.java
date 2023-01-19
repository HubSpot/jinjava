package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PyishObjectMapper {
  public static final ObjectWriter PYISH_OBJECT_WRITER;

  static {
    ObjectMapper mapper = new ObjectMapper(
      new JsonFactoryBuilder().quoteChar('\'').build()
    );

    mapper =
      mapper.registerModule(
        new SimpleModule()
          .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
          .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
      );
    mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
    PYISH_OBJECT_WRITER =
      mapper.writer(PyishPrettyPrinter.INSTANCE).with(PyishCharacterEscapes.INSTANCE);
  }

  public static String getAsUnquotedPyishString(Object val) {
    if (val != null) {
      return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val));
    }
    return "";
  }

  public static String getAsPyishString(Object val) {
    try {
      return getAsPyishStringOrThrow(val);
    } catch (IOException e) {
      if (e instanceof SizeLimitingJsonProcessingException) {
        throw new DeferredValueException(String.format("%s: %s", e.getMessage(), val));
      }
      return Objects.toString(val, "");
    }
  }

  public static String getAsPyishStringOrThrow(Object val) throws IOException {
    ObjectWriter objectWriter = PYISH_OBJECT_WRITER;
    Writer writer;
    Optional<Long> maxOutputSize = JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getConfig().getMaxOutputSize())
      .filter(max -> max > 0);
    if (maxOutputSize.isPresent()) {
      AtomicInteger remainingLength = new AtomicInteger(
        (int) Math.min(Integer.MAX_VALUE, maxOutputSize.get())
      );
      objectWriter =
        objectWriter.withAttribute(
          SizeLimitingWriter.REMAINING_LENGTH_ATTRIBUTE,
          remainingLength
        );
      writer = new SizeLimitingWriter(new CharArrayWriter(), remainingLength);
    } else {
      writer = new CharArrayWriter();
    }
    objectWriter.writeValue(writer, val);
    return writer.toString();
  }

  public static class NullKeySerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(
      Object o,
      JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider
    )
      throws IOException {
      jsonGenerator.writeFieldName("");
    }
  }
}
