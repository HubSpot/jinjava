package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.JinjavaImmutableStyle;
import com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy;
import com.hubspot.jinjava.lib.expression.ExpressionStrategy;
import javax.annotation.Nullable;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable(singleton = true)
@JinjavaImmutableStyle
public interface ContextConfiguration extends WithContextConfiguration {
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
  default boolean isPartialMacroEvaluation() {
    return false;
  }

  @Default
  default boolean isUnwrapRawOverride() {
    return false;
  }

  @Default
  default ErrorHandlingStrategy getErrorHandlingStrategy() {
    return ImmutableErrorHandlingStrategy.of();
  }

  static ContextConfiguration of() {
    return ImmutableContextConfiguration.of();
  }
}
