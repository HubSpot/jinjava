package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.features.DateTimeFeatureActivationStrategy;
import com.hubspot.jinjava.features.DelegatingFeatureActivationStrategy;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.features.Features;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

public class FeaturesTest {
  private static final String ALWAYS_OFF = "alwaysOff";
  private static final String ALWAYS_ON = "alwaysOn";
  private static final String DATE_PAST = "datePast";
  private static final String DATE_FUTURE = "dateFuture";
  private static final String DELEGATING = "delegating";

  private Features features;

  private boolean delegateActive = false;

  @Before
  public void setUp() throws Exception {
    features =
      new Features(
        FeatureConfig
          .newBuilder()
          .add(ALWAYS_OFF, FeatureStrategies.ALWAYS_OFF)
          .add(ALWAYS_ON, FeatureStrategies.ALWAYS_ON)
          .add(DATE_PAST, DateTimeFeatureActivationStrategy.of(LocalDateTime.MIN))
          .add(DATE_FUTURE, DateTimeFeatureActivationStrategy.of(LocalDateTime.MAX))
          .add(DELEGATING, DelegatingFeatureActivationStrategy.of(() -> delegateActive))
          .build()
      );
  }

  @Test
  public void itHasEnabledFeature() {
    assertThat(features.isActive(ALWAYS_ON)).isTrue();
  }

  @Test
  public void itDoesNotHaveDisabledFeature() {
    assertThat(features.isActive(ALWAYS_OFF)).isFalse();
  }

  @Test
  public void itHasPastEnabledFeature() {
    assertThat(features.isActive(DATE_PAST)).isTrue();
  }

  @Test
  public void itDoesNotHaveFutureEnabledFeature() {
    assertThat(features.isActive(DATE_FUTURE)).isFalse();
  }

  @Test
  public void itUsesDelegate() {
    delegateActive = false;
    assertThat(features.isActive(DELEGATING)).isEqualTo(delegateActive);
    delegateActive = true;
    assertThat(features.isActive(DELEGATING)).isEqualTo(delegateActive);
  }

  @Test
  public void itDefaultsToFalse() {
    assertThat(features.isActive("unknown")).isFalse();
  }
}
