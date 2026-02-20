package com.hubspot.jinjava.el.ext;

import javax.annotation.Nullable;

public interface ReturnTypeValidator {
  @Nullable
  Object validateReturnType(Object o);

  boolean allowReturnTypeClass(Class<?> c);
}
