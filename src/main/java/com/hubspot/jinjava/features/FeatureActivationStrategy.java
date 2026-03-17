package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;

public interface FeatureActivationStrategy {
  default boolean isActive(Context context) {
    return isActive();
  }

  boolean isActive();
}
