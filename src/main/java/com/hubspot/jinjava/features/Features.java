package com.hubspot.jinjava.features;

public class Features {
  private final FeatureConfig featureConfig;

  public Features(FeatureConfig featureConfig) {
    this.featureConfig = featureConfig;
  }

  public boolean isActive(String featureName) {
    return getActivationStrategy(featureName).isActive();
  }

  public FeatureActivationStrategy getActivationStrategy(String featureName) {
    return featureConfig.getFeature(featureName);
  }
}
