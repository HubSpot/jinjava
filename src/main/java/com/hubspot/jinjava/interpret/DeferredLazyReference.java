package com.hubspot.jinjava.interpret;

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
  public Object getOriginalValue() {
    return lazyReference;
  }
}
