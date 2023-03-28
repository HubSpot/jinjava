package com.hubspot.jinjava.interpret;

/**
 * Marker object which indicates that the template engine should skip over evaluating
 * this part of the template, if the object is resolved from the context.
 *
 */
public interface DeferredValue {
  Object getOriginalValue();

  static DeferredValue instance() {
    return DeferredValueImpl.instance();
  }

  static DeferredValue instance(Object originalValue) {
    return DeferredValueImpl.instance(originalValue);
  }

  static DeferredValueShadow shadowInstance(Object originalValue) {
    return DeferredValueShadow.instance(originalValue);
  }
}
