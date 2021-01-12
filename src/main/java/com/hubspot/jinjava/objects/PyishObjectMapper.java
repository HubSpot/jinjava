package com.hubspot.jinjava.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PyishObjectMapper {
  private final PyishObjectMapper parent;
  private Set<Class<?>> defaultJsonClasses;
  private ObjectMapper objectMapper;

  public PyishObjectMapper(PyishObjectMapper parent) {
    this.parent = parent;
    if (parent == null) {
      this.defaultJsonClasses = new HashSet<>();
      registerDefaults();
      objectMapper =
        new ObjectMapper()
        .registerModule(
            new SimpleModule()
            .setSerializerModifier(new PyishBeanSerializerModifier(defaultJsonClasses))
          );
    }
  }

  private void registerDefaults() {
    registerClasses(Map.class, Collection.class);
  }

  public void registerClasses(Class<?>... classes) {
    if (parent != null) {
      throw new UnsupportedOperationException();
    }
    defaultJsonClasses.addAll(Arrays.asList(classes));
  }

  public ObjectMapper getObjectMapper() {
    if (parent != null) {
      return parent.getObjectMapper();
    }
    return objectMapper;
  }

  public String getAsUnquotedPyishString(Object val) {
    if (val != null) {
      return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val));
    }
    return "";
  }

  public String getAsPyishString(Object val) {
    try {
      return getObjectMapper()
        .writeValueAsString(val)
        .replace("'", "\\'")
        // Replace `\n` with a newline character
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\\\\n)", "$1\n")
        // Replace double-quotes with single quote as they are preferred in Jinja
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\")", "$1'")
        // Replace escaped backslash with backslash character
        // because object mapper escapes slashes.
        .replace("\\\\", "\\");
    } catch (JsonProcessingException e) {
      return Objects.toString(val, "");
    }
  }
}
