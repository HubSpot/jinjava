package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public interface DepthAndWidthLimiting<T> {
  String REMAINING_DEPTH_KEY = "remainingDepth";
  String REMAINING_WIDTH_KEY = "remainingWidth";

  static void checkDepthAndWidth(SerializerProvider provider, ThrowingRunnable action)
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

  default void serialize(
    T object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    checkDepthAndWidth(
      serializerProvider,
      () -> innerSerialize(object, jsonGenerator, serializerProvider)
    );
  }

  void innerSerialize(
    T object,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException;

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Throwable;
  }
}
