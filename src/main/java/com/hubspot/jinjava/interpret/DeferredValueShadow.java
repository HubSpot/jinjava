package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

/**
 * A deferred value which represents that a value was deferred within this context,
 * but it is does not overwrite the actual key in which the original value resides on the context.
 */
@Beta
public class DeferredValueShadow extends DeferredValueImpl {

  protected DeferredValueShadow() {}

  protected DeferredValueShadow(Object originalValue) {
    super(originalValue);
  }

  protected static DeferredValueShadow instance() {
    return new DeferredValueShadow();
  }

  protected static DeferredValueShadow instance(Object originalValue) {
    return new DeferredValueShadow(originalValue);
  }
}
