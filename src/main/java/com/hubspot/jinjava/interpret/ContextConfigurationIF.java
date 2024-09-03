package com.hubspot.jinjava.interpret;

import com.hubspot.immutables.style.HubSpotImmutableStyle;
import com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy;
import com.hubspot.jinjava.lib.expression.ExpressionStrategy;
import javax.annotation.Nullable;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable(singleton = true)
@HubSpotImmutableStyle
public interface ContextConfigurationIF {
  @Default
  default ExpressionStrategy getExpressionStrategy() {
    return new DefaultExpressionStrategy();
  }

  @Nullable
  DynamicVariableResolver getDynamicVariableResolver();

  @Default
  default boolean isValidationMode() {
    return false;
  }

  @Default
  default boolean isDeferredExecutionMode() {
    return false;
  }

  @Default
  default boolean isDeferLargeObjects() {
    return false;
  }

  @Default
  default boolean isThrowInterpreterErrors() {
    return false;
  }

  @Default
  default boolean isPartialMacroEvaluation() {
    return false;
  }

  @Default
  default boolean isUnwrapRawOverride() {
    return false;
  }
}
