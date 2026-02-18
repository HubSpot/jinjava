package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.ConcurrentHashMap;

public final class AllowlistReturnTypeValidator {

  private final ConcurrentHashMap<String, Boolean> allowedReturnTypesCache;

  private final ImmutableSet<String> allowedCanonicalClassPrefixes;
  private final ImmutableSet<String> allowedCanonicalClassNames;
  private final ImmutableList<ReturnTypeValidator> additionalValidators;

  public static AllowlistReturnTypeValidator create(
    ReturnTypeValidatorConfig returnTypeValidatorConfig,
    ReturnTypeValidator... additionalValidators
  ) {
    return new AllowlistReturnTypeValidator(
      returnTypeValidatorConfig,
      ImmutableList.copyOf(additionalValidators)
    );
  }

  private AllowlistReturnTypeValidator(
    ReturnTypeValidatorConfig returnTypeValidatorConfig,
    ImmutableList<ReturnTypeValidator> additionalValidators
  ) {
    this.allowedCanonicalClassPrefixes =
      returnTypeValidatorConfig.allowedCanonicalClassPrefixes();
    this.allowedCanonicalClassNames =
      returnTypeValidatorConfig.allowedCanonicalClassNames();
    this.additionalValidators = additionalValidators;
    this.allowedReturnTypesCache = new ConcurrentHashMap<>();
  }

  public Object validateReturnType(Object o) {
    if (o == null) {
      return null;
    }
    Class<?> clazz = o.getClass();
    while (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }
    String canonicalClassName = clazz.getCanonicalName();
    boolean isAllowedClassName = allowedReturnTypesCache.computeIfAbsent(
      canonicalClassName,
      c ->
        allowedCanonicalClassNames.contains(canonicalClassName) ||
        allowedCanonicalClassPrefixes.stream().anyMatch(canonicalClassName::startsWith)
    );
    if (!isAllowedClassName) {
      return null;
    }
    for (ReturnTypeValidator v : additionalValidators) {
      o = v.validateReturnType(o);
      if (o == null) {
        return null;
      }
    }
    return o;
  }
}
