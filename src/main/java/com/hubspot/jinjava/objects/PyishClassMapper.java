package com.hubspot.jinjava.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PyishClassMapper {
  private final PyishClassMapper parent;
  private Set<Class<?>> defaultJsonClasses;
  private ObjectMapper objectMapper;

  public PyishClassMapper(PyishClassMapper parent) {
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
}
