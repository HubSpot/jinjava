package com.hubspot.jinjava.el.ext;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

public interface MethodValidator {
  @Nullable
  Method validateMethod(Method m);
}
