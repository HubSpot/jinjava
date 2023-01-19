package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PyishObjectMapper {
  public static final ObjectWriter PYISH_OBJECT_WRITER;

  static {
    ObjectMapper mapper = new ObjectMapper(
      new JsonFactoryBuilder().quoteChar('\'').build()
    );
    mapper.setSerializerFactory(DepthAndWidthLimitingSerializerFactory.instance);

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
    } catch (JsonProcessingException e) {
      return Objects.toString(val, "");
    }
  }

  public static String getAsPyishStringOrThrow(Object val)
    throws JsonProcessingException {
    String string = PYISH_OBJECT_WRITER
      .withAttribute(
        DepthAndWidthLimitingSerializerFactory.DEPTH_KEY,
        new AtomicInteger(6)
      )
      .withAttribute(
        DepthAndWidthLimitingSerializerFactory.WIDTH_KEY,
        new AtomicInteger(10)
      )
      .writeValueAsString(val);
    JinjavaInterpreter.checkOutputSize(string);
    return string;
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
