package com.hubspot.jinjava.features;

import java.util.function.Supplier;

public class DelegatingFeatureActivationStrategy implements FeatureActivationStrategy {
  public Supplier<Boolean> delegate;

  public static FeatureActivationStrategy of(Supplier<Boolean> delegate) {
    return new DelegatingFeatureActivationStrategy(delegate);
  }

  private DelegatingFeatureActivationStrategy(Supplier<Boolean> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean isActive() {
    return delegate.get();
  }
}
