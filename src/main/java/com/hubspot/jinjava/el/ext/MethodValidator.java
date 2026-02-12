package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;

public class MethodValidator {

  private final ImmutableSet<Method> allowedMethods;
  private final ImmutableSet<Class<?>> allowedDeclaredMethodsFromClasses;

  public MethodValidator(
    ImmutableSet<Method> allowedMethods,
    ImmutableSet<Class<?>> allowedDeclaredMethodsFromClasses
  ) {
    this.allowedMethods = allowedMethods;
    this.allowedDeclaredMethodsFromClasses = allowedDeclaredMethodsFromClasses;
  }

  public Method validateMethod(Method m) {
    return (
        allowedMethods.contains(m) ||
        allowedDeclaredMethodsFromClasses.contains(m.getDeclaringClass())
      )
      ? m
      : null;
  }
}
