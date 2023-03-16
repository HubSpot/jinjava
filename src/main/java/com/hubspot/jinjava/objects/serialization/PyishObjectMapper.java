package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
  public static final ObjectWriter SNAKE_CASE_PYISH_OBJECT_WRITER;
  public static final String ALLOW_SNAKE_CASE_ATTRIBUTE = "allowSnakeCase";

  static {
    PYISH_OBJECT_WRITER =
      getPyishObjectMapper()
        .writer(PyishPrettyPrinter.INSTANCE)
        .with(PyishCharacterEscapes.INSTANCE);

    SNAKE_CASE_PYISH_OBJECT_WRITER =
      getPyishObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .writer(PyishPrettyPrinter.INSTANCE)
        .with(PyishCharacterEscapes.INSTANCE);
  }

  private static ObjectMapper getPyishObjectMapper() {
    ObjectMapper mapper = new ObjectMapper(
      new JsonFactoryBuilder().quoteChar('\'').build()
    )
    .registerModule(
        new SimpleModule()
          .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
          .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
      );
    mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
    return mapper;
  }

  public static String getAsUnquotedPyishString(Object val) {
    if (val != null) {
      return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val, true));
    }
    return "";
  }

  public static String getAsPyishString(Object val) {
    return getAsPyishString(val, false);
  }

  private static String getAsPyishString(Object val, boolean forOutput) {
    try {
      return getAsPyishStringOrThrow(val, forOutput);
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
    return getAsPyishStringOrThrow(val, false);
  }

  public static String getAsPyishStringOrThrow(Object val, boolean forOutput)
    throws IOException {
    boolean useSnakeCaseMappingOverride = JinjavaInterpreter
      .getCurrentMaybe()
      .map(
        interpreter ->
          interpreter.getConfig().getLegacyOverrides().isUseSnakeCasePropertyNaming()
      )
      .orElse(false);
    ObjectWriter objectWriter = useSnakeCaseMappingOverride
      ? SNAKE_CASE_PYISH_OBJECT_WRITER
      : PYISH_OBJECT_WRITER;
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
    if (!useSnakeCaseMappingOverride) {
      objectWriter = objectWriter.withAttribute(ALLOW_SNAKE_CASE_ATTRIBUTE, !forOutput);
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
