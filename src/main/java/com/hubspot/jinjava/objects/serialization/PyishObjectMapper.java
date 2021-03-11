package com.hubspot.jinjava.objects.serialization;

import static com.hubspot.jinjava.objects.serialization.PyishSerializable.PYISH_OBJECT_WRITER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Objects;

public class PyishObjectMapper {

  public static String getAsUnquotedPyishString(Object val) {
    if (val != null) {
      return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val));
    }
    return "";
  }

  public static String getAsPyishString(Object val) {
    try {
      return PYISH_OBJECT_WRITER
        .writeValueAsString(val)
        .replace("'", "\\'")
        // Replace double-quotes with single quote as they are preferred in Jinja
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\")", "$1'");
    } catch (JsonProcessingException e) {
      return Objects.toString(val, "");
    }
  }
}
