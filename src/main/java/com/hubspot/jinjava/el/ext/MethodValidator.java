package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;

public final class MethodValidator {

  private final ImmutableSet<Method> allowedMethods;
  private final ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassPrefixes;
  private final ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassNames;
  private final ImmutableSet<String> allowedResultCanonicalClassPrefixes;
  private final ImmutableSet<String> allowedResultCanonicalClassNames;

  public static MethodValidator create(MethodValidatorConfig methodValidatorConfig) {
    return new MethodValidator(methodValidatorConfig);
  }

  private MethodValidator(MethodValidatorConfig methodValidatorConfig) {
    this.allowedMethods = methodValidatorConfig.allowedMethods();
    this.allowedDeclaredMethodsFromCanonicalClassPrefixes =
      methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassPrefixes();
    this.allowedDeclaredMethodsFromCanonicalClassNames =
      methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassNames();
    this.allowedResultCanonicalClassPrefixes =
      ImmutableSet
        .<String>builder()
        .addAll(methodValidatorConfig.allowedResultCanonicalClassPrefixes())
        .addAll(methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassPrefixes())
        .build();
    this.allowedResultCanonicalClassNames =
      ImmutableSet
        .<String>builder()
        .addAll(methodValidatorConfig.allowedResultCanonicalClassNames())
        .addAll(methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassNames())
        .build();
  }

  public Method validateMethod(Method m) {
    if (m == null) {
      return null;
    }
    String canonicalDeclaringClassName = m.getDeclaringClass().getCanonicalName();
    return (
        allowedMethods.contains(m) ||
        allowedDeclaredMethodsFromCanonicalClassNames.contains(
          canonicalDeclaringClassName
        ) ||
        allowedDeclaredMethodsFromCanonicalClassPrefixes
          .stream()
          .anyMatch(canonicalDeclaringClassName::startsWith)
      )
      ? m
      : null;
  }

  public Object validateResult(Object o) {
    if (o == null) {
      return null;
    }
    String canonicalClassName = o.getClass().getCanonicalName();
    return (
        allowedResultCanonicalClassNames.contains(canonicalClassName) ||
        allowedResultCanonicalClassPrefixes
          .stream()
          .anyMatch(canonicalClassName::startsWith)
      )
      ? o
      : null;
  }
}
