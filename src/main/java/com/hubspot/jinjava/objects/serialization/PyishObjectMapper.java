package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Objects;

public class PyishObjectMapper {
  public static final ObjectWriter PYISH_OBJECT_WRITER = new ObjectMapper()
    .registerModule(
      new SimpleModule()
        .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
        .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
    )
    .writer(PyishPrettyPrinter.INSTANCE)
    .with(PyishCharacterEscapes.INSTANCE);

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
}
