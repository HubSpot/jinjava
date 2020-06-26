package com.hubspot.jinjava.interpret;

/**
 * Marker object which indicates that the template engine should skip over evaluating
 * this part of the template, if the object is resolved from the context.
 *
 */
public class DeferredValue {
  private static final DeferredValue INSTANCE = new DeferredValue();

  private Object originalValue;

  private DeferredValue() {}

  private DeferredValue(Object originalValue) {
    this.originalValue = originalValue;
  }

  public Object getOriginalValue() {
    return originalValue;
  }

  public static DeferredValue instance() {
    return INSTANCE;
  }

  public static DeferredValue instance(Object originalValue) {
    return new DeferredValue(originalValue);
  }
}
