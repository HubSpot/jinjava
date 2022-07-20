package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;

public class LazyReference extends LazyExpression implements PyishSerializable {
  private final String referenceKey;

  protected LazyReference(Context referenceContext, String referenceKey) {
    super(() -> referenceContext.get(referenceKey), "", Memoization.ON);
    get();
    this.referenceKey = referenceKey;
  }

  public static LazyReference of(Context referenceContext, String referenceKey) {
    return new LazyReference(referenceContext, referenceKey);
  }

  public String getReferenceKey() {
    return referenceKey;
  }

  @Override
  public String toPyishString() {
    return getReferenceKey();
  }
}
