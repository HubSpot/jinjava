package com.hubspot.jinjava.features;

public class FeatureStrategies {
  public static final FeatureActivationStrategy INACTIVE = c -> false;
  public static final FeatureActivationStrategy ACTIVE = c -> true;
}
