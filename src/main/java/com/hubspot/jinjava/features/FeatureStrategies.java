package com.hubspot.jinjava.features;

public class FeatureStrategies {
  public static final FeatureActivationStrategy ALWAYS_OFF = DelegatingFeatureActivationStrategy.of(
    () -> false
  );
  public static final FeatureActivationStrategy ALWAYS_ON = DelegatingFeatureActivationStrategy.of(
    () -> true
  );
}
