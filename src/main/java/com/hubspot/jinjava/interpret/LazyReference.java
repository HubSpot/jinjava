package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;

public class LazyReference extends LazyExpression implements PyishSerializable {

  private String referenceKey;

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

  public void setReferenceKey(String referenceKey) {
    this.referenceKey = referenceKey;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) appendable.append(getReferenceKey());
  }
}
