package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.MapEntrySerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Defaults;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class DepthAndWidthLimitingSerializerFactory extends BeanSerializerFactory {
  public static final DepthAndWidthLimitingSerializerFactory instance = new DepthAndWidthLimitingSerializerFactory(
    null
  );
  public static final String DEPTH_KEY = "maxDepth";
  public static final String WIDTH_KEY = "maxWidth";

  protected DepthAndWidthLimitingSerializerFactory(SerializerFactoryConfig config) {
    super(config);
  }

  public static void checkDepthAndWidth(
    SerializerProvider provider,
    ThrowingRunnable action
  )
    throws IOException {
    try {
      AtomicInteger depth = (AtomicInteger) provider.getAttribute(DEPTH_KEY);
      AtomicInteger width = (AtomicInteger) provider.getAttribute(WIDTH_KEY);
      if (width.decrementAndGet() >= 0) {
        if (depth.decrementAndGet() >= 0) {
          action.run();
          depth.incrementAndGet();
        }
      }
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected JsonSerializer<?> buildContainerSerializer(
    SerializerProvider prov,
    JavaType type,
    BeanDescription beanDesc,
    boolean staticTyping
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildContainerSerializer(prov, type, beanDesc, staticTyping)
    );
  }

  @Override
  protected JsonSerializer<?> buildCollectionSerializer(
    SerializerProvider prov,
    CollectionType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    TypeSerializer elementTypeSerializer,
    JsonSerializer<Object> elementValueSerializer
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildCollectionSerializer(
        prov,
        type,
        beanDesc,
        staticTyping,
        elementTypeSerializer,
        elementValueSerializer
      )
    );
  }

  @Override
  public ContainerSerializer<?> buildIndexedListSerializer(
    JavaType elemType,
    boolean staticTyping,
    TypeSerializer vts,
    JsonSerializer<Object> valueSerializer
  ) {
    return getLimitingSerializer(
      super.buildIndexedListSerializer(elemType, staticTyping, vts, valueSerializer)
    );
  }

  @Override
  public ContainerSerializer<?> buildCollectionSerializer(
    JavaType elemType,
    boolean staticTyping,
    TypeSerializer vts,
    JsonSerializer<Object> valueSerializer
  ) {
    return getLimitingSerializer(
      super.buildCollectionSerializer(elemType, staticTyping, vts, valueSerializer)
    );
  }

  @Override
  protected JsonSerializer<?> buildMapSerializer(
    SerializerProvider prov,
    MapType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    JsonSerializer<Object> keySerializer,
    TypeSerializer elementTypeSerializer,
    JsonSerializer<Object> elementValueSerializer
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildMapSerializer(
        prov,
        type,
        beanDesc,
        staticTyping,
        keySerializer,
        elementTypeSerializer,
        elementValueSerializer
      )
    );
  }

  @Override
  protected JsonSerializer<?> buildMapEntrySerializer(
    SerializerProvider prov,
    JavaType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    JavaType keyType,
    JavaType valueType
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildMapEntrySerializer(
        prov,
        type,
        beanDesc,
        staticTyping,
        keyType,
        valueType
      )
    );
  }

  @Override
  protected JsonSerializer<?> buildArraySerializer(
    SerializerProvider prov,
    ArrayType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    TypeSerializer elementTypeSerializer,
    JsonSerializer<Object> elementValueSerializer
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildArraySerializer(
        prov,
        type,
        beanDesc,
        staticTyping,
        elementTypeSerializer,
        elementValueSerializer
      )
    );
  }

  @Override
  protected JsonSerializer<?> buildIteratorSerializer(
    SerializationConfig config,
    JavaType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    JavaType valueType
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildIteratorSerializer(config, type, beanDesc, staticTyping, valueType)
    );
  }

  @Override
  protected JsonSerializer<?> buildIterableSerializer(
    SerializationConfig config,
    JavaType type,
    BeanDescription beanDesc,
    boolean staticTyping,
    JavaType valueType
  )
    throws JsonMappingException {
    return getLimitingSerializer(
      super.buildIterableSerializer(config, type, beanDesc, staticTyping, valueType)
    );
  }

  @Override
  public SerializerFactory withConfig(SerializerFactoryConfig config) {
    if (this._factoryConfig == config) {
      return this;
    } else if (this.getClass() != DepthAndWidthLimitingSerializerFactory.class) {
      throw new IllegalStateException(
        "Subtype of DepthAndWidthLimitingSerializerFactory (" +
        this.getClass().getName() +
        ") has not properly overridden method 'withAdditionalSerializers': cannot instantiate subtype with additional serializer definitions"
      );
    } else {
      return new DepthAndWidthLimitingSerializerFactory(config);
    }
  }

  private static <T extends JsonSerializer<?>> T getLimitingSerializer(T serializer) {
    // TODO better check
    if (serializer instanceof DepthAndWidthLimiting) {
      return serializer;
    }
    Class<?> clazz = serializer.getClass();
    while (Modifier.isFinal(clazz.getModifiers())) {
      clazz = clazz.getSuperclass();
    }
    final Class<?> finalClazz = clazz;
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(finalClazz);
    factory.setFilter(
      method ->
        isSerializeMethod(method) || method.getReturnType().isAssignableFrom(finalClazz)
    );
    factory.setInterfaces(new Class[] { DepthAndWidthLimiting.class });

    MethodHandler handler = (self, thisMethod, proceed, args) -> {
      if (isSerializeMethod(thisMethod)) {
        SerializerProvider serializerProvider = (SerializerProvider) args[2];
        checkDepthAndWidth(
          serializerProvider,
          () -> {
            if (serializer instanceof MapEntrySerializer) {
              WrappingMapEntrySerializer.serialize(
                (Entry<?, ?>) args[0],
                (JsonGenerator) args[1],
                (SerializerProvider) args[2]
              );
            } else {
              thisMethod.invoke(serializer, args);
            }
          }
        );
        return null;
      } else {
        return getLimitingSerializer((T) thisMethod.invoke(serializer, args));
      }
    };
    try {
      Class<?>[] parameterTypes = finalClazz
        .getDeclaredConstructors()[0].getParameterTypes();

      return (
        (T) factory.create(
          parameterTypes,
          Arrays
            .stream(parameterTypes)
            .map(
              type -> {
                if (JavaType.class.isAssignableFrom(type)) {
                  return SimpleType.constructUnsafe(serializer.handledType());
                } else if (finalClazz.isAssignableFrom(type)) {
                  return serializer;
                }
                return Defaults.defaultValue(type);
              }
            )
            .toArray(),
          handler
        )
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isSerializeMethod(Method method) {
    return (
      (
        "serialize".equals(method.getName()) ||
        "serializeWithType".equals(method.getName())
      ) &&
      void.class.equals(method.getReturnType())
    );
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Throwable;
  }
}
