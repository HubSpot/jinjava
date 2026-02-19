package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Beta
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
      .registerModule(new Jdk8Module())
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
      handleLengthLimitingException(e);
      handleDeferredValueException(e);
      return Objects.toString(val, "");
    }
  }

  private static void handleDeferredValueException(IOException e) {
    Throwable unwrapped = e;
    if (e instanceof JsonMappingException) {
      unwrapped = unwrapped.getCause();
    }
    if (unwrapped instanceof DeferredValueException) {
      throw (DeferredValueException) unwrapped;
    }
  }

  public static void handleLengthLimitingException(IOException e) {
    Throwable unwrapped = e;
    if (e instanceof JsonMappingException) {
      unwrapped = unwrapped.getCause();
    }
    if (unwrapped instanceof LengthLimitingJsonProcessingException) {
      throw new OutputTooBigException(
        ((LengthLimitingJsonProcessingException) unwrapped).getMaxSize(),
        ((LengthLimitingJsonProcessingException) unwrapped).getAttemptedSize()
      );
    } else if (unwrapped instanceof OutputTooBigException) {
      throw (OutputTooBigException) unwrapped;
    }
  }

  public static String getAsPyishStringOrThrow(Object val) throws IOException {
    return getAsPyishStringOrThrow(val, false);
  }

  public static String getAsPyishStringOrThrow(Object val, boolean forOutput)
    throws IOException {
    boolean useSnakeCaseMappingOverride = JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter ->
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
    ) throws IOException {
      jsonGenerator.writeFieldName("");
    }
  }
}
