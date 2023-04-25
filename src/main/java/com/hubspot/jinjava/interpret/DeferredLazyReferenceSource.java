package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

@Beta
public class DeferredLazyReferenceSource extends DeferredValueImpl {
  private static final DeferredLazyReferenceSource INSTANCE = new DeferredLazyReferenceSource();

  private boolean reconstructed;

  private DeferredLazyReferenceSource() {}

  private DeferredLazyReferenceSource(Object originalValue) {
    super(originalValue);
  }

  public static DeferredLazyReferenceSource instance() {
    return INSTANCE;
  }

  public static DeferredLazyReferenceSource instance(Object originalValue) {
    return new DeferredLazyReferenceSource(originalValue);
  }

  public boolean isReconstructed() {
    return reconstructed;
  }

  public void setReconstructed(boolean reconstructed) {
    this.reconstructed = reconstructed;
  }
}
