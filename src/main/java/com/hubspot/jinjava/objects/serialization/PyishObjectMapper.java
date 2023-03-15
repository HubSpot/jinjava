package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PyishObjectMapper {
  public static final ObjectWriter PYISH_OBJECT_WRITER;
  public static final String EAGER_EXECUTION_ATTRIBUTE = "eagerExecution";

  static {
    ObjectMapper mapper = new ObjectMapper(
      new JsonFactoryBuilder().quoteChar('\'').build()
    )
    .registerModule(
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
      if (e instanceof LengthLimitingJsonProcessingException) {
        throw new OutputTooBigException(
          ((LengthLimitingJsonProcessingException) e).getMaxSize(),
          ((LengthLimitingJsonProcessingException) e).getAttemptedSize()
        );
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
          LengthLimitingWriter.REMAINING_LENGTH_ATTRIBUTE,
          remainingLength
        );
      writer = new LengthLimitingWriter(new CharArrayWriter(), remainingLength);
    } else {
      writer = new CharArrayWriter();
    }
    objectWriter
      .withAttribute(
        EAGER_EXECUTION_ATTRIBUTE,
        JinjavaInterpreter
          .getCurrentMaybe()
          .map(interpreter -> interpreter.getConfig().getExecutionMode().useEagerParser())
          .orElse(false)
      )
      .writeValue(writer, val);
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
