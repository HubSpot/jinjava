package com.hubspot.jinjava;

import org.immutables.value.Value;

/**
 * This class allows Jinjava to be configured to override legacy behaviour.
 * LegacyOverrides.NONE signifies that none of the legacy functionality will be overridden.
 * LegacyOverrides.ALL signifies that all new functionality will be used; avoid legacy "bugs".
 */
@Value.Immutable(singleton = true)
@JinjavaImmutableStyle.WithStyle
public interface LegacyOverrides extends WithLegacyOverrides {
  LegacyOverrides NONE = new Builder().build();
  LegacyOverrides ALL = new Builder()
    .withEvaluateMapKeys(true)
    .withIterateOverMapKeys(true)
    .withUsePyishObjectMapper(true)
    .withUseSnakeCasePropertyNaming(true)
    .withWhitespaceRequiredWithinTokens(true)
    .withUseNaturalOperatorPrecedence(true)
    .withParseWhitespaceControlStrictly(true)
    .withAllowAdjacentTextNodes(true)
    .withUseTrimmingForNotesAndExpressions(true)
    .withKeepNullableLoopValues(true)
    .build();

  @Value.Default
  default boolean isEvaluateMapKeys() {
    return false;
  }

  @Value.Default
  default boolean isIterateOverMapKeys() {
    return false;
  }

  @Value.Default
  default boolean isUsePyishObjectMapper() {
    return false;
  }

  @Value.Default
  default boolean isUseSnakeCasePropertyNaming() {
    return false;
  }

  @Value.Default
  default boolean isWhitespaceRequiredWithinTokens() {
    return false;
  }

  @Value.Default
  default boolean isUseNaturalOperatorPrecedence() {
    return false;
  }

  @Value.Default
  default boolean isParseWhitespaceControlStrictly() {
    return false;
  }

  @Value.Default
  default boolean isAllowAdjacentTextNodes() {
    return false;
  }

  @Value.Default
  default boolean isUseTrimmingForNotesAndExpressions() {
    return false;
  }

  @Value.Default
  default boolean isKeepNullableLoopValues() {
    return false;
  }

  class Builder extends ImmutableLegacyOverrides.Builder {}

  static Builder newBuilder() {
    return builder();
  }

  static Builder builder() {
    return new Builder();
  }
}
