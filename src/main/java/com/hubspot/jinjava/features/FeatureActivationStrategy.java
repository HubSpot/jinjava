package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;

public interface FeatureActivationStrategy {
  boolean isActive(Context context);
}
