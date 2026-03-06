package com.hubspot.jinjava.el.ext;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MethodValidator {
  @Nullable
  Method validateMethod(@Nonnull Method m);
}
