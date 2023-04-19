package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

@Beta
public class DeferredLazyReference
  implements DeferredValue, Cloneable, OneTimeReconstructible {
  private final LazyReference lazyReference;
  private boolean reconstructed;

  private DeferredLazyReference(LazyReference lazyReference) {
    this.lazyReference = lazyReference;
  }

  private DeferredLazyReference(Context referenceContext, String referenceKey) {
    lazyReference = LazyReference.of(referenceContext, referenceKey);
  }

  public static DeferredLazyReference instance(
    Context referenceContext,
    String referenceKey
  ) {
    return new DeferredLazyReference(referenceContext, referenceKey);
  }

  @Override
  public LazyReference getOriginalValue() {
    return lazyReference;
  }

  public boolean isReconstructed() {
    return reconstructed;
  }

  public void setReconstructed(boolean reconstructed) {
    this.reconstructed = reconstructed;
  }

  @Override
  public DeferredLazyReference clone() {
    try {
      return (DeferredLazyReference) super.clone();
    } catch (CloneNotSupportedException e) {
      return new DeferredLazyReference(lazyReference);
    }
  }
}
