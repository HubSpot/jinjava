package com.hubspot.jinjava.features;

public class FeatureStrategies {

  public static final FeatureActivationStrategy INACTIVE = () -> false;
  public static final FeatureActivationStrategy ACTIVE = () -> true;
}
