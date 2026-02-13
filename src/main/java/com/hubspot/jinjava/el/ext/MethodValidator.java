package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
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
    Class<?> clazz = m.getDeclaringClass();
    String canonicalClassName = clazz.getCanonicalName();
    if (
      allowedMethods.contains(m) ||
      allowedDeclaredMethodsFromCanonicalClassNames.contains(canonicalClassName) ||
      allowedDeclaredMethodsFromCanonicalClassPrefixes
        .stream()
        .anyMatch(canonicalClassName::startsWith)
    ) {
      return m;
    }
    return null;
  }

  public Object validateResult(Object o) {
    Object wrapped = JinjavaInterpreter
      .getCurrentMaybe()
      .map(jinjavaInterpreter -> jinjavaInterpreter.wrap(o))
      .orElse(o);

    if (wrapped == null) {
      return null;
    }
    Class<?> clazz = wrapped.getClass();
    String canonicalClassName = clazz.getCanonicalName();
    if (
      allowedResultCanonicalClassNames.contains(canonicalClassName) ||
      allowedResultCanonicalClassPrefixes
        .stream()
        .anyMatch(canonicalClassName::startsWith)
    ) {
      return wrapped;
    }
    return null;
  }
}
