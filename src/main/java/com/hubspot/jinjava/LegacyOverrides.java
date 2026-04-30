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
    .withHandleBackslashInQuotesOnly(true)
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
    .withHandleBackslashInQuotesOnly(true)
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

  /**
   * When {@code true}, the token scanner treats backslash as an escape character
   * only inside quoted string literals, leaving bare backslashes outside quotes
   * untouched for the expression parser (JUEL) to handle. This matches the
   * behaviour of Python's Jinja2, where the template scanner is not responsible
   * for backslash interpretation at all.
   *
   * <p>When {@code false} (the default), the scanner consumes a backslash and
   * the following character unconditionally, regardless of quote context. This
   * is the legacy Jinjava behaviour, which prevents closing delimiters from
   * being recognized after a backslash but diverges from Jinja2.
   */
  @Value.Default
  default boolean isHandleBackslashInQuotesOnly() {
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
