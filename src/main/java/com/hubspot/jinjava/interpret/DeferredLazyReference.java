package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

@Beta
public class DeferredLazyReference implements DeferredValue {
  private final LazyReference lazyReference;

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
}
