package com.hubspot.jinjava.features;

public class DisabledFeatureActivationStrategy implements FeatureActivationStrategy {
  private static final DisabledFeatureActivationStrategy INSTANCE = new DisabledFeatureActivationStrategy();

  @Override
  public boolean isActive() {
    return false;
  }

  public static DisabledFeatureActivationStrategy getInstance() {
    return INSTANCE;
  }
}
