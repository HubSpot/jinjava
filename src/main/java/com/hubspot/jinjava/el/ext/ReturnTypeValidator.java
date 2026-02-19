package com.hubspot.jinjava.el.ext;

public interface ReturnTypeValidator {
  Object validateReturnType(Object o);
  boolean allowReturnTypeClass(Class<?> c);
}
