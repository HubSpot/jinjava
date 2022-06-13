package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class PyishBeanSerializerModifier extends BeanSerializerModifier {
  public static final PyishBeanSerializerModifier INSTANCE = new PyishBeanSerializerModifier();

  private PyishBeanSerializerModifier() {}

  @Override
  public JsonSerializer<?> modifySerializer(
    SerializationConfig config,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    // Use the PyishSerializer if it extends the PyishSerializable class.
    // For example, a Map implementation could then have custom string serialization.
    if (!(PyishSerializable.class.isAssignableFrom(beanDesc.getBeanClass()))) {
      return serializer;
    } else {
      return PyishSerializer.INSTANCE;
    }
  }
}
