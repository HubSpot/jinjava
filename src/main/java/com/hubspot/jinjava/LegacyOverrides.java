package com.hubspot.jinjava;

/**
 * This class allows Jinjava to be configured to override legacy behaviour.
 * LegacyOverrides.NONE signifies that none of the legacy functionality will be overridden.
 * LegacyOverrides.ALL signifies that all new functionality will be used; avoid legacy "bugs".
 */
public class LegacyOverrides {

  public static final LegacyOverrides NONE = new LegacyOverrides.Builder().build();
  public static final LegacyOverrides THREE_POINT_0 = new LegacyOverrides.Builder()
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
  public static final LegacyOverrides ALL = new LegacyOverrides.Builder()
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
  }
}
