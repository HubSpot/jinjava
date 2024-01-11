package com.hubspot.jinjava.features;

import com.hubspot.jinjava.interpret.Context;
import java.time.ZonedDateTime;

public class DateTimeFeatureActivationStrategy implements FeatureActivationStrategy {

  private final ZonedDateTime activateAt;

  public static DateTimeFeatureActivationStrategy of(ZonedDateTime activateAt) {
    return new DateTimeFeatureActivationStrategy(activateAt);
  }

  private DateTimeFeatureActivationStrategy(ZonedDateTime activateAt) {
    this.activateAt = activateAt;
  }

  @Override
  public boolean isActive(Context context) {
    return ZonedDateTime.now().isAfter(activateAt);
  }

  public ZonedDateTime getActivateAt() {
    return activateAt;
  }
}
