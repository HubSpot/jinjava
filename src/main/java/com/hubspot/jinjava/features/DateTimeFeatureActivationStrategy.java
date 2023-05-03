package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;
import java.time.LocalDateTime;

public class DateTimeFeatureActivationStrategy implements FeatureActivationStrategy {
  private final LocalDateTime activateAt;

  public static DateTimeFeatureActivationStrategy of(LocalDateTime activateAt) {
    return new DateTimeFeatureActivationStrategy(activateAt);
  }

  private DateTimeFeatureActivationStrategy(LocalDateTime activateAt) {
    this.activateAt = activateAt;
  }

  @Override
  public boolean isActive(Context context) {
    return LocalDateTime.now().isAfter(activateAt);
  }

  public LocalDateTime getActivateAt() {
    return activateAt;
  }
}
