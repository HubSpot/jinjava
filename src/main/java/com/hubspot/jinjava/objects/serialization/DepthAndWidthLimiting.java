package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface DepthAndWidthLimiting<T> {
  String DEPTH_AND_WIDTH_TRACKER = "depthAndWidthTracker";

  static void checkDepthAndWidth(SerializerProvider provider, ThrowingRunnable action)
    throws IOException {
    try {
      DepthAndWidthTracker tracker = (DepthAndWidthTracker) provider.getAttribute(
        DEPTH_AND_WIDTH_TRACKER
      );
      if (tracker != null) {
        if (tracker.acquire()) {
          action.run();
          tracker.release();
        } else {
          throw new DepthAndWidthLimitingException(tracker);
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
