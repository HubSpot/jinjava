package com.hubspot.jinjava.interpret;

/**
 * Marker object which indicates that the template engine should skip over evaluating
 * this part of the template, if the object is resolved from the context.
 *
 */
public class DeferredValue {
  private static final DeferredValue INSTANCE = new DeferredValue();

  private DeferredValue() {
  }

  public static DeferredValue instance() {
    return INSTANCE;
  }
}
