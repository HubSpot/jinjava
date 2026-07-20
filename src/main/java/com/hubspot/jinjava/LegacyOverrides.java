package com.hubspot.jinjava;

/**
 * This class allows Jinjava to be configured to override legacy behaviour.
 * LegacyOverrides.NONE signifies that none of the legacy functionality will be overridden.
 * LegacyOverrides.ALL signifies that all new functionality will be used; avoid legacy "bugs".
 */
public class LegacyOverrides {

  public static final LegacyOverrides NONE = new LegacyOverrides.Builder().build();
  public static final LegacyOverrides ALL = new LegacyOverrides.Builder()
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
    .withHandleBackslashInQuotesOnly(true)
    .withDefaultKeepTrailingNewlineBehavior(false)
    .build();
  private final boolean evaluateMapKeys;
  private final boolean iterateOverMapKeys;
  private final boolean usePyishObjectMapper;
  private final boolean useSnakeCasePropertyNaming;
  private final boolean whitespaceRequiredWithinTokens;
  private final boolean useNaturalOperatorPrecedence;
  private final boolean parseWhitespaceControlStrictly;
  private final boolean allowAdjacentTextNodes;
  private final boolean useTrimmingForNotesAndExpressions;
  private final boolean keepNullableLoopValues;
  private final boolean handleBackslashInQuotesOnly;
  private final boolean defaultKeepTrailingNewlineBehavior;

  private LegacyOverrides(Builder builder) {
    evaluateMapKeys = builder.evaluateMapKeys;
    iterateOverMapKeys = builder.iterateOverMapKeys;
    usePyishObjectMapper = builder.usePyishObjectMapper;
    useSnakeCasePropertyNaming = builder.useSnakeCasePropertyNaming;
    whitespaceRequiredWithinTokens = builder.whitespaceRequiredWithinTokens;
    useNaturalOperatorPrecedence = builder.useNaturalOperatorPrecedence;
    parseWhitespaceControlStrictly = builder.parseWhitespaceControlStrictly;
    allowAdjacentTextNodes = builder.allowAdjacentTextNodes;
    useTrimmingForNotesAndExpressions = builder.useTrimmingForNotesAndExpressions;
    keepNullableLoopValues = builder.keepNullableLoopValues;
    handleBackslashInQuotesOnly = builder.handleBackslashInQuotesOnly;
    defaultKeepTrailingNewlineBehavior = builder.defaultKeepTrailingNewlineBehavior;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public boolean isEvaluateMapKeys() {
    return evaluateMapKeys;
  }

  public boolean isIterateOverMapKeys() {
    return iterateOverMapKeys;
  }

  public boolean isUsePyishObjectMapper() {
    return usePyishObjectMapper;
  }

  public boolean isUseSnakeCasePropertyNaming() {
    return useSnakeCasePropertyNaming;
  }

  public boolean isWhitespaceRequiredWithinTokens() {
    return whitespaceRequiredWithinTokens;
  }

  public boolean isUseNaturalOperatorPrecedence() {
    return useNaturalOperatorPrecedence;
  }

  public boolean isParseWhitespaceControlStrictly() {
    return parseWhitespaceControlStrictly;
  }

  public boolean isAllowAdjacentTextNodes() {
    return allowAdjacentTextNodes;
  }

  public boolean isUseTrimmingForNotesAndExpressions() {
    return useTrimmingForNotesAndExpressions;
  }

  public boolean isKeepNullableLoopValues() {
    return keepNullableLoopValues;
  }

  public boolean isHandleBackslashInQuotesOnly() {
    return handleBackslashInQuotesOnly;
  }

  /**
   * The default value of {@link JinjavaConfig#isKeepTrailingNewline()}.
   * {@code true} preserves Jinjava's historical behaviour of keeping the trailing newline;
   * {@code false} matches Python Jinja2's default of stripping it.
   */
  public boolean getDefaultKeepTrailingNewlineBehavior() {
    return defaultKeepTrailingNewlineBehavior;
  }

  public static class Builder {

    private boolean evaluateMapKeys = false;
    private boolean iterateOverMapKeys = false;
    private boolean usePyishObjectMapper = false;
    private boolean useSnakeCasePropertyNaming = false;
    private boolean whitespaceRequiredWithinTokens = false;
    private boolean useNaturalOperatorPrecedence = false;
    private boolean parseWhitespaceControlStrictly = false;
    private boolean allowAdjacentTextNodes = false;
    private boolean useTrimmingForNotesAndExpressions = false;
    private boolean keepNullableLoopValues = false;
    private boolean handleBackslashInQuotesOnly = false;
    private boolean defaultKeepTrailingNewlineBehavior = true;

    private Builder() {}

    public LegacyOverrides build() {
      return new LegacyOverrides(this);
    }

    public static Builder from(LegacyOverrides legacyOverrides) {
      return new Builder()
        .withEvaluateMapKeys(legacyOverrides.evaluateMapKeys)
        .withIterateOverMapKeys(legacyOverrides.iterateOverMapKeys)
        .withUsePyishObjectMapper(legacyOverrides.usePyishObjectMapper)
        .withUseSnakeCasePropertyNaming(legacyOverrides.useSnakeCasePropertyNaming)
        .withWhitespaceRequiredWithinTokens(
          legacyOverrides.whitespaceRequiredWithinTokens
        )
        .withUseNaturalOperatorPrecedence(legacyOverrides.useNaturalOperatorPrecedence)
        .withParseWhitespaceControlStrictly(
          legacyOverrides.parseWhitespaceControlStrictly
        )
        .withAllowAdjacentTextNodes(legacyOverrides.allowAdjacentTextNodes)
        .withUseTrimmingForNotesAndExpressions(
          legacyOverrides.useTrimmingForNotesAndExpressions
        )
        .withHandleBackslashInQuotesOnly(legacyOverrides.handleBackslashInQuotesOnly)
        .withDefaultKeepTrailingNewlineBehavior(
          legacyOverrides.defaultKeepTrailingNewlineBehavior
        );
    }

    public Builder withEvaluateMapKeys(boolean evaluateMapKeys) {
      this.evaluateMapKeys = evaluateMapKeys;
      return this;
    }

    public Builder withIterateOverMapKeys(boolean iterateOverMapKeys) {
      this.iterateOverMapKeys = iterateOverMapKeys;
      return this;
    }

    public Builder withUsePyishObjectMapper(boolean usePyishObjectMapper) {
      this.usePyishObjectMapper = usePyishObjectMapper;
      return this;
    }

    public Builder withUseSnakeCasePropertyNaming(boolean useSnakeCasePropertyNaming) {
      this.useSnakeCasePropertyNaming = useSnakeCasePropertyNaming;
      return this;
    }

    /**
     * Use {@link com.hubspot.jinjava.features.BuiltInFeatures#WHITESPACE_REQUIRED_WITHIN_TOKENS} instead
     */
    @Deprecated
    public Builder withWhitespaceRequiredWithinTokens(
      boolean whitespaceRequiredWithinTokens
    ) {
      this.whitespaceRequiredWithinTokens = whitespaceRequiredWithinTokens;
      return this;
    }

    public Builder withUseNaturalOperatorPrecedence(
      boolean useNaturalOperatorPrecedence
    ) {
      this.useNaturalOperatorPrecedence = useNaturalOperatorPrecedence;
      return this;
    }

    public Builder withParseWhitespaceControlStrictly(
      boolean parseWhitespaceControlStrictly
    ) {
      this.parseWhitespaceControlStrictly = parseWhitespaceControlStrictly;
      return this;
    }

    public Builder withAllowAdjacentTextNodes(boolean allowAdjacentTextNodes) {
      this.allowAdjacentTextNodes = allowAdjacentTextNodes;
      return this;
    }

    public Builder withUseTrimmingForNotesAndExpressions(
      boolean useTrimmingForNotesAndExpressions
    ) {
      this.useTrimmingForNotesAndExpressions = useTrimmingForNotesAndExpressions;
      return this;
    }

    public Builder withKeepNullableLoopValues(boolean keepNullableLoopValues) {
      this.keepNullableLoopValues = keepNullableLoopValues;
      return this;
    }

    public Builder withHandleBackslashInQuotesOnly(boolean handleBackslashInQuotesOnly) {
      this.handleBackslashInQuotesOnly = handleBackslashInQuotesOnly;
      return this;
    }

    public Builder withDefaultKeepTrailingNewlineBehavior(
      boolean defaultKeepTrailingNewlineBehavior
    ) {
      this.defaultKeepTrailingNewlineBehavior = defaultKeepTrailingNewlineBehavior;
      return this;
    }
  }
}
