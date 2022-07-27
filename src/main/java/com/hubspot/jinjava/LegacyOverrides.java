package com.hubspot.jinjava;

/**
 * This class allows Jinjava to be configured to override legacy behaviour.
 * LegacyOverrides.NONE signifies that none of the legacy functionality will be overridden.
 */
public class LegacyOverrides {
  public static final LegacyOverrides NONE = new LegacyOverrides.Builder().build();
  private final boolean evaluateMapKeys;
  private final boolean iterateOverMapKeys;
  private final boolean usePyishObjectMapper;
  private final boolean whitespaceRequiredWithinTokens;
  private final boolean useNaturalOperatorPrecedence;
  private final boolean parseWhitespaceControlStrictly;

  private LegacyOverrides(Builder builder) {
    evaluateMapKeys = builder.evaluateMapKeys;
    iterateOverMapKeys = builder.iterateOverMapKeys;
    usePyishObjectMapper = builder.usePyishObjectMapper;
    whitespaceRequiredWithinTokens = builder.whitespaceRequiredWithinTokens;
    useNaturalOperatorPrecedence = builder.useNaturalOperatorPrecedence;
    parseWhitespaceControlStrictly = builder.parseWhitespaceControlStrictly;
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

  public boolean isWhitespaceRequiredWithinTokens() {
    return whitespaceRequiredWithinTokens;
  }

  public boolean isUseNaturalOperatorPrecedence() {
    return useNaturalOperatorPrecedence;
  }

  public boolean isParseWhitespaceControlStrictly() {
    return parseWhitespaceControlStrictly;
  }

  public static class Builder {
    private boolean evaluateMapKeys = false;
    private boolean iterateOverMapKeys = false;
    private boolean usePyishObjectMapper = false;
    private boolean whitespaceRequiredWithinTokens = false;
    private boolean useNaturalOperatorPrecedence = false;
    private boolean parseWhitespaceControlStrictly = false;

    private Builder() {}

    public LegacyOverrides build() {
      return new LegacyOverrides(this);
    }

    public static Builder from(LegacyOverrides legacyOverrides) {
      return new Builder()
        .withEvaluateMapKeys(legacyOverrides.evaluateMapKeys)
        .withIterateOverMapKeys(legacyOverrides.iterateOverMapKeys)
        .withUsePyishObjectMapper(legacyOverrides.usePyishObjectMapper)
        .withWhitespaceRequiredWithinTokens(
          legacyOverrides.whitespaceRequiredWithinTokens
        )
        .withUseNaturalOperatorPrecedence(legacyOverrides.useNaturalOperatorPrecedence)
        .withParseWhitespaceControlStrictly(
          legacyOverrides.parseWhitespaceControlStrictly
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
  }
}
