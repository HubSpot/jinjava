package com.hubspot.jinjava.features;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

public class FeatureConfig {

  Map<String, FeatureActivationStrategy> features;

  private FeatureConfig(Map<String, FeatureActivationStrategy> features) {
    this.features = ImmutableMap.copyOf(features);
  }

  public FeatureActivationStrategy getFeature(String name) {
    return features.getOrDefault(name, FeatureStrategies.INACTIVE);
  }

  public static FeatureConfig.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private final Map<String, FeatureActivationStrategy> features = new HashMap<>();

    public Builder add(String name, FeatureActivationStrategy strategy) {
      features.put(name, strategy);
      return this;
    }

    public FeatureConfig build() {
      return new FeatureConfig(features);
    }
  }
}
