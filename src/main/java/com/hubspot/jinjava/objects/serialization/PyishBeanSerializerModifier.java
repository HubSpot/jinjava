package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.Map;

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
      if (Map.Entry.class.isAssignableFrom(beanDesc.getBeanClass())) {
        return MapEntrySerializer.INSTANCE;
      }
      if (
        serializer instanceof BeanSerializer &&
        Boolean.TRUE.equals(
          config.getAttributes().getAttribute(PyishObjectMapper.EAGER_EXECUTION_ATTRIBUTE)
        )
      ) {
        return BothCasingBeanSerializer.INSTANCE;
      }
      return serializer;
    } else {
      return PyishSerializer.INSTANCE;
    }
  }
}
