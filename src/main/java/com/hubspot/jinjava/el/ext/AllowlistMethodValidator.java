package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;

public final class AllowlistMethodValidator {

  private final ImmutableSet<Method> allowedMethods;
  private final ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassPrefixes;
  private final ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassNames;
  private final ImmutableList<MethodValidator> additionalValidators;

  public static AllowlistMethodValidator create(
    MethodValidatorConfig methodValidatorConfig,
    MethodValidator... additionalValidators
  ) {
    return new AllowlistMethodValidator(
      methodValidatorConfig,
      ImmutableList.copyOf(additionalValidators)
    );
  }

  private AllowlistMethodValidator(
    MethodValidatorConfig methodValidatorConfig,
    ImmutableList<MethodValidator> additionalValidators
  ) {
    this.allowedMethods = methodValidatorConfig.allowedMethods();
    this.allowedDeclaredMethodsFromCanonicalClassPrefixes =
      methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassPrefixes();
    this.allowedDeclaredMethodsFromCanonicalClassNames =
      methodValidatorConfig.allowedDeclaredMethodsFromCanonicalClassNames();
    this.additionalValidators = additionalValidators;
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
      for (MethodValidator v : additionalValidators) {
        m = v.validateMethod(m);
        if (m == null) {
          return null;
        }
      }
      return m;
    }
    return null;
  }
}
