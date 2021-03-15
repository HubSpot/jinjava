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

  private LegacyOverrides(Builder builder) {
    evaluateMapKeys = builder.evaluateMapKeys;
    iterateOverMapKeys = builder.iterateOverMapKeys;
    usePyishObjectMapper = builder.usePyishObjectMapper;
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

  public static class Builder {
    private boolean evaluateMapKeys = false;
    private boolean iterateOverMapKeys = false;
    private boolean usePyishObjectMapper = false;

    private Builder() {}

    public LegacyOverrides build() {
      return new LegacyOverrides(this);
    }

    public static Builder from(LegacyOverrides legacyOverrides) {
      return new Builder()
        .withEvaluateMapKeys(legacyOverrides.evaluateMapKeys)
        .withIterateOverMapKeys(legacyOverrides.iterateOverMapKeys)
        .withUsePyishObjectMapper(legacyOverrides.usePyishObjectMapper);
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
  }
}
