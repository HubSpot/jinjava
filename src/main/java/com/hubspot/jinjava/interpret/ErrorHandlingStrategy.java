package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.JinjavaImmutableStyle;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public interface ErrorHandlingStrategy {
  @Value.Default
  default TemplateErrorTypeHandlingStrategy getFatalErrorStrategy() {
    return TemplateErrorTypeHandlingStrategy.ADD_ERROR;
  }

  @Value.Default
  default TemplateErrorTypeHandlingStrategy getNonFatalErrorStrategy() {
    return TemplateErrorTypeHandlingStrategy.ADD_ERROR;
  }

  enum TemplateErrorTypeHandlingStrategy {
    IGNORE,
    ADD_ERROR,
    THROW_EXCEPTION,
  }

  class Builder extends ImmutableErrorHandlingStrategy.Builder {}

  static Builder builder() {
    return new Builder();
  }

  static ErrorHandlingStrategy throwAll() {
    return ErrorHandlingStrategy
      .builder()
      .setFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.THROW_EXCEPTION)
      .setNonFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.THROW_EXCEPTION)
      .build();
  }

  static ErrorHandlingStrategy ignoreAll() {
    return ErrorHandlingStrategy
      .builder()
      .setFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.IGNORE)
      .setNonFatalErrorStrategy(TemplateErrorTypeHandlingStrategy.IGNORE)
      .build();
  }
}
