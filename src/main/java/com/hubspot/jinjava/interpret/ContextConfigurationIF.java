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
  default boolean isPartialMacroEvaluation() {
    return false;
  }

  @Default
  default boolean isPreserveResolvedSetTags() {
    return false;
  }

  @Default
  default boolean isUnwrapRawOverride() {
    return false;
  }

  @Default
  default ErrorHandlingStrategy getErrorHandlingStrategy() {
    return ErrorHandlingStrategy.of();
  }

  @Immutable(singleton = true)
  @HubSpotImmutableStyle
  interface ErrorHandlingStrategyIF {
    @Default
    default TemplateErrorTypeHandlingStrategy getFatalErrorStrategy() {
      return TemplateErrorTypeHandlingStrategy.ADD_ERROR;
    }

    @Default
    default TemplateErrorTypeHandlingStrategy getNonFatalErrorStrategy() {
      return TemplateErrorTypeHandlingStrategy.ADD_ERROR;
    }

    enum TemplateErrorTypeHandlingStrategy {
      IGNORE,
      ADD_ERROR,
      THROW_EXCEPTION,
    }

    static ErrorHandlingStrategy throwAll() {
      return ErrorHandlingStrategy
        .of()
        .withFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.THROW_EXCEPTION)
        .withNonFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.THROW_EXCEPTION);
    }

    static ErrorHandlingStrategy ignoreAll() {
      return ErrorHandlingStrategy
        .of()
        .withFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.IGNORE)
        .withNonFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.IGNORE);
    }
  }
}
