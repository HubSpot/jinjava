package com.hubspot.jinjava.features;

public class FeatureStrategies {
  public static final FeatureActivationStrategy INACTIVE = DelegatingFeatureActivationStrategy.of(
    () -> false
  );
  public static final FeatureActivationStrategy ACTIVE = DelegatingFeatureActivationStrategy.of(
    () -> true
  );
}
