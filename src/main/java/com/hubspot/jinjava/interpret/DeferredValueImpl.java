package com.hubspot.jinjava.interpret;

import java.util.Objects;

public class DeferredValueImpl implements DeferredValue {
  private static final DeferredValue INSTANCE = new DeferredValueImpl();

  private Object originalValue;

  protected DeferredValueImpl() {}

  protected DeferredValueImpl(Object originalValue) {
    this.originalValue = originalValue;
  }

  @Override
  public Object getOriginalValue() {
    return originalValue;
  }

  protected static DeferredValue instance() {
    return INSTANCE;
  }

  protected static DeferredValue instance(Object originalValue) {
    return new DeferredValueImpl(originalValue);
  }

  @Override
  public String toString() {
    return Objects.toString(originalValue);
  }
}
