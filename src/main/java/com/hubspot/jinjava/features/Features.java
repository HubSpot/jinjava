package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;

public class Features {

  private final FeatureConfig featureConfig;

  public Features(FeatureConfig featureConfig) {
    this.featureConfig = featureConfig;
  }

  public boolean isActive(String featureName, Context context) {
    return getActivationStrategy(featureName).isActive(context);
  }

  public boolean isActive(String featureName) {
    return getActivationStrategy(featureName).isActive();
  }

  public FeatureActivationStrategy getActivationStrategy(String featureName) {
    return featureConfig.getFeature(featureName);
  }
}
