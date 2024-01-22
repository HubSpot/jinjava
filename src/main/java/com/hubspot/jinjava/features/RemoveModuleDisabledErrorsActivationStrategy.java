package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;

public class RemoveModuleDisabledErrorsActivationStrategy implements FeatureActivationStrategy{
  private final boolean enabled;

  public static RemoveModuleDisabledErrorsActivationStrategy of(boolean enabled) {
    return new RemoveModuleDisabledErrorsActivationStrategy(enabled);
  }
  private RemoveModuleDisabledErrorsActivationStrategy(boolean enabled) {
    this.enabled = enabled;
  }


  @Override
  public boolean isActive(Context context) {
    return enabled;
  }
}
