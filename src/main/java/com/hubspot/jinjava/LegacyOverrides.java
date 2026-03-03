package com.hubspot.jinjava;

import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle.WithStyle
public interface LegacyOverrides extends WithLegacyOverrides {
  LegacyOverrides NONE = new Builder().build();
  LegacyOverrides THREE_POINT_0 = new Builder()
    .withEvaluateMapKeys(true)
    .withIterateOverMapKeys(true)
    .withUsePyishObjectMapper(true)
    .withUseSnakeCasePropertyNaming(true)
    .withUseNaturalOperatorPrecedence(true)
    .withParseWhitespaceControlStrictly(true)
    .withAllowAdjacentTextNodes(true)
    .withUseTrimmingForNotesAndExpressions(true)
    .withKeepNullableLoopValues(true)
    .build();
  LegacyOverrides ALL = new Builder()
    .withEvaluateMapKeys(true)
    .withIterateOverMapKeys(true)
    .withUsePyishObjectMapper(true)
    .withUseSnakeCasePropertyNaming(true)
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
    return new Builder();
  }
}
