package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.IOException;
import java.util.Objects;

public class PyishObjectMapper {
  public static final ObjectWriter PYISH_OBJECT_WRITER;

  static {
    ObjectMapper mapper = new ObjectMapper()
      .registerModule(
        new SimpleModule()
          .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
          .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
      )
      .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
      .setSerializationInclusion(Include.NON_NULL);
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
      String string = PYISH_OBJECT_WRITER
        .writeValueAsString(val)
        .replace("'", "\\'")
        // Replace double-quotes with single quote as they are preferred in Jinja
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\")", "$1'");
      if (!string.contains("{{")) {
        return String.join("} ", string.split("}(?=})"));
      }
      return string;
    } catch (JsonProcessingException e) {
      return Objects.toString(val, "");
    }
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
