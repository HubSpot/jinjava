package com.hubspot.jinjava.features;

public class StaticFeatureActivationStrategy implements FeatureActivationStrategy {
  private final boolean active;

  public static StaticFeatureActivationStrategy of(boolean active) {
    return new StaticFeatureActivationStrategy(active);
  }

  private StaticFeatureActivationStrategy(boolean active) {
    this.active = active;
  }

  @Override
  public boolean isActive() {
    return active;
  }
}
