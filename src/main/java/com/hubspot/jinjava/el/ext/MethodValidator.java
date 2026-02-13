package com.hubspot.jinjava.el.ext;

import java.lang.reflect.Method;

public interface MethodValidator {
  Method validateMethod(Method m);
}
