package com.hubspot.jinjava.interpret;

public class DeferredValueMarker extends DeferredValueImpl {
  private static final DeferredValueMarker INSTANCE = new DeferredValueMarker();

  protected DeferredValueMarker() {}

  protected DeferredValueMarker(Object originalValue) {
    super(originalValue);
  }

  public static DeferredValueMarker instance() {
    return INSTANCE;
  }

  public static DeferredValueMarker instance(Object originalValue) {
    return new DeferredValueMarker(originalValue);
  }
}
