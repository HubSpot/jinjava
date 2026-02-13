package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public final class AllowlistReturnTypeValidator {

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
  }

  public Object validateReturnType(Object o) {
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
      allowedCanonicalClassNames.contains(canonicalClassName) ||
      allowedCanonicalClassPrefixes.stream().anyMatch(canonicalClassName::startsWith)
    ) {
      for (ReturnTypeValidator v : additionalValidators) {
        wrapped = v.validateReturnType(wrapped);
        if (wrapped == null) {
          return null;
        }
      }
      return wrapped;
    }
    return null;
  }
}
