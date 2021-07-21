package com.hubspot.jinjava.interpret;

import java.util.Objects;

public class DeferredValueImpl implements DeferredValue {
  private static final DeferredValue INSTANCE = new DeferredValueImpl();

  private Object originalValue;

  private DeferredValueImpl() {}

  private DeferredValueImpl(Object originalValue) {
    this.originalValue = originalValue;
  }

  public Object getOriginalValue() {
    return originalValue;
  }

  public static DeferredValue instance() {
    return INSTANCE;
  }

  public static DeferredValue instance(Object originalValue) {
    return new DeferredValueImpl(originalValue);
  }

  @Override
  public String toString() {
    return Objects.toString(originalValue);
  }
}
