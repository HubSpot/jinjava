package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;

public final class MethodValidator {

  private final ImmutableSet<Method> allowedMethods;
  private final ImmutableSet<Class<?>> allowedDeclaredMethodsFromClasses;
  private final ImmutableSet<String> allowedDeclaredMethodsFromPackages;
  private final ImmutableSet<Class<?>> allowedResultClasses;
  private final ImmutableSet<String> allowedResultPackages;

  public static MethodValidator create(MethodValidatorConfig methodValidatorConfig) {
    return new MethodValidator(methodValidatorConfig);
  }

  private MethodValidator(MethodValidatorConfig methodValidatorConfig) {
    this.allowedMethods = methodValidatorConfig.allowedMethods();
    this.allowedDeclaredMethodsFromClasses =
      methodValidatorConfig.allowedDeclaredMethodsFromClasses();
    this.allowedDeclaredMethodsFromPackages =
      methodValidatorConfig.allowedDeclaredMethodsFromPackages();
    this.allowedResultClasses = methodValidatorConfig.allowedResultClasses();
    this.allowedResultPackages = methodValidatorConfig.allowedResultPackages();
  }

  public Method validateMethod(Method m) {
    return (
        m == null ||
        allowedMethods.contains(m) ||
        allowedDeclaredMethodsFromClasses.contains(m.getDeclaringClass()) ||
        allowedDeclaredMethodsFromPackages
          .stream()
          .anyMatch(p -> m.getDeclaringClass().getPackageName().startsWith(p))
      )
      ? m
      : null;
  }

  public Object validateResult(Object o) {
    return (
      o == null ||
      allowedResultClasses.contains(o.getClass()) ||
      allowedResultPackages
        .stream()
        .anyMatch(p -> o.getClass().getPackageName().startsWith(p))
    );
  }
}
