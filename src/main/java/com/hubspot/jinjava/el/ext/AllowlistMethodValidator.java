package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public final class AllowlistMethodValidator {

  private final ConcurrentHashMap<Method, Boolean> allowedMethodsCache;
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
    this.allowedMethodsCache = new ConcurrentHashMap<>();
  }

  public Method validateMethod(Method m) {
    if (m == null) {
      return null;
    }
    boolean isAllowedMethod = allowedMethodsCache.computeIfAbsent(
      m,
      m1 -> {
        Class<?> clazz = m1.getDeclaringClass();
        String canonicalClassName = clazz.getCanonicalName();
        return (
          allowedMethods.contains(m1) ||
          allowedDeclaredMethodsFromCanonicalClassNames.contains(canonicalClassName) ||
          allowedDeclaredMethodsFromCanonicalClassPrefixes
            .stream()
            .anyMatch(canonicalClassName::startsWith)
        );
      }
    );
    if (!isAllowedMethod) {
      return null;
    }
    for (MethodValidator v : additionalValidators) {
      m = v.validateMethod(m);
      if (m == null) {
        return null;
      }
    }

    return m;
  }
}
