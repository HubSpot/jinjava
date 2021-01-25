package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PyishObjectMapper {
  private final PyishObjectMapper parent;
  private Set<Class<?>> nonPyishClasses;
  private ObjectWriter objectWriter;

  public PyishObjectMapper() {
    this(null);
  }

  public PyishObjectMapper(PyishObjectMapper parent) {
    this.parent = parent;
    if (parent == null) {
      this.nonPyishClasses = new HashSet<>();
      registerDefaults();
    }
  }

  private void registerDefaults() {
    registerNonPyishClasses(Map.class);
  }

  /**
   * Reigisters classes that are serialized to a string using jackson's default
   * json serialization rather than calling toString on the object.
   * @param classes Classes that don't have a pythonic <code>toString()</code> implementation
   */
  public void registerNonPyishClasses(Class<?>... classes) {
    if (parent != null) {
      throw new UnsupportedOperationException();
    }
    nonPyishClasses.addAll(Arrays.asList(classes));
    updateObjectWriter();
  }

  public String getAsUnquotedPyishString(Object val) {
    if (val != null) {
      return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val));
    }
    return "";
  }

  public String getAsPyishString(Object val) {
    try {
      return getObjectWriter()
        .writeValueAsString(val)
        .replace("'", "\\'")
        // Replace `\n` with a newline character
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\\\\n)", "$1\n")
        // Replace double-quotes with single quote as they are preferred in Jinja
        .replaceAll("(?<!\\\\)(\\\\\\\\)*(?:\")", "$1'");
    } catch (JsonProcessingException e) {
      return Objects.toString(val, "");
    }
  }

  private void updateObjectWriter() {
    objectWriter =
      new ObjectMapper()
        .registerModule(
          new SimpleModule()
          .setSerializerModifier(new PyishBeanSerializerModifier(nonPyishClasses))
        )
        .writer(PyishPrettyPrinter.INSTANCE);
  }

  private ObjectWriter getObjectWriter() {
    if (parent != null) {
      return parent.getObjectWriter();
    }
    return objectWriter;
  }
}
