package com.hubspot.jinjava.objects;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.Set;

public class PyishBeanSerializerModifier extends BeanSerializerModifier {
  private static final JsonSerializer<?> PYISH_JSON_SERIALIZER = new PyishJsonSerializer();
  private final Set<Class<?>> defaultClasses;

  public PyishBeanSerializerModifier(Set<Class<?>> defaultClasses) {
    this.defaultClasses = defaultClasses;
  }

  @Override
  public JsonSerializer<?> modifySerializer(
    SerializationConfig config,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    if (
      defaultClasses
        .stream()
        .anyMatch(clazz -> (clazz.isAssignableFrom(beanDesc.getBeanClass())))
    ) {
      return serializer;
    }
    return PYISH_JSON_SERIALIZER;
  }
}
