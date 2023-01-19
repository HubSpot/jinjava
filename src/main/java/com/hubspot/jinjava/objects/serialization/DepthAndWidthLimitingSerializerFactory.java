package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Defaults;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class DepthAndWidthLimitingSerializerFactory {
  public static final SerializerFactory instance = getSerializerFactory(
    BeanSerializerFactory.instance
  );
  public static final String REMAINING_DEPTH_KEY = "remainingDepth";
  public static final String REMAINING_WIDTH_KEY = "remainingWidth";

  public static void checkDepthAndWidth(
    SerializerProvider provider,
    ThrowingRunnable action
  )
    throws IOException {
    try {
      AtomicInteger depth = (AtomicInteger) provider.getAttribute(REMAINING_DEPTH_KEY);
      AtomicInteger width = (AtomicInteger) provider.getAttribute(REMAINING_WIDTH_KEY);
      if (width != null && depth != null) {
        if (width.decrementAndGet() >= 0 && depth.decrementAndGet() >= 0) {
          action.run();
          depth.incrementAndGet();
        } else {
          throw new DepthAndWidthLimitingException(depth);
        }
      } else {
        action.run();
      }
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      if (
        e instanceof InvocationTargetException &&
        (
          (InvocationTargetException) e
        ).getTargetException() instanceof DepthAndWidthLimitingException
      ) {
        throw (DepthAndWidthLimitingException) (
          (InvocationTargetException) e
        ).getTargetException();
      }
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static SerializerFactory getSerializerFactory(SerializerFactory delegate) {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(SerializerFactory.class);
    factory.setFilter(method -> Modifier.isPublic(method.getModifiers()));
    MethodHandler handler = (self, thisMethod, proceed, args) -> {
      if (returnsJsonSerializer(thisMethod)) {
        return getLimitingSerializer(
          (JsonSerializer<?>) thisMethod.invoke(delegate, args)
        );
      } else if (returnsSerializerFactory(thisMethod)) {
        return getSerializerFactory(
          (SerializerFactory) thisMethod.invoke(delegate, args)
        );
      } else {
        return thisMethod.invoke(delegate, args);
      }
    };
    try {
      return ((SerializerFactory) factory.create(new Class[0], new Object[0], handler));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean returnsJsonSerializer(Method method) {
    return JsonSerializer.class.isAssignableFrom(method.getReturnType());
  }

  private static boolean returnsSerializerFactory(Method method) {
    return SerializerFactory.class.isAssignableFrom(method.getReturnType());
  }

  @SuppressWarnings("unchecked")
  private static <T extends JsonSerializer<?>> T getLimitingSerializer(T serializer)
    throws NoSuchMethodException {
    if (serializer instanceof DepthAndWidthLimiting) {
      return serializer;
    }
    Class<?> clazz = serializer.getClass();
    while (
      Modifier.isFinal(clazz.getModifiers()) ||
      Modifier.isFinal(
        clazz
          .getMethod(
            "serialize",
            Object.class,
            JsonGenerator.class,
            SerializerProvider.class
          )
          .getModifiers()
      )
    ) {
      clazz = clazz.getSuperclass();
    }
    final Class<?> finalClazz = clazz;
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(finalClazz);
    factory.setFilter(method -> Modifier.isPublic(method.getModifiers()));
    factory.setInterfaces(new Class[] { DepthAndWidthLimiting.class });

    MethodHandler handler = (self, thisMethod, proceed, args) -> {
      if (isSerializeMethod(thisMethod)) {
        checkDepthAndWidth(
          (SerializerProvider) args[2],
          () -> thisMethod.invoke(serializer, args)
        );
        return null;
      } else if (thisMethod.getReturnType().isAssignableFrom(finalClazz)) {
        return getLimitingSerializer((T) thisMethod.invoke(serializer, args));
      } else {
        return thisMethod.invoke(serializer, args);
      }
    };
    try {
      Class<?>[] parameterTypes = getConstructor(finalClazz).getParameterTypes();
      return (
        (T) factory.create(
          parameterTypes,
          Arrays
            .stream(parameterTypes)
            .map(
              type -> {
                if (JavaType.class.isAssignableFrom(type)) {
                  return SimpleType.constructUnsafe(serializer.handledType());
                } else if (
                  JsonSerializer.class.isAssignableFrom(type) ||
                  type.isAssignableFrom(finalClazz)
                ) {
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

  private static Constructor<?> getConstructor(Class<?> finalClazz) {
    return Arrays
      .stream(finalClazz.getDeclaredConstructors())
      .filter(
        constructor ->
          Arrays
            .stream(constructor.getParameterTypes())
            .noneMatch(AnnotatedMember.class::isAssignableFrom)
      )
      .findAny()
      .orElseGet(() -> finalClazz.getDeclaredConstructors()[0]);
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
