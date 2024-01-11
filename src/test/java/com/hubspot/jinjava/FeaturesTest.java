package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.features.DateTimeFeatureActivationStrategy;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.features.Features;
import com.hubspot.jinjava.interpret.Context;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

  private Context context = new Context();

  @Before
  public void setUp() throws Exception {
    features =
      new Features(
        FeatureConfig
          .newBuilder()
          .add(ALWAYS_OFF, FeatureStrategies.INACTIVE)
          .add(ALWAYS_ON, FeatureStrategies.ACTIVE)
          .add(
            DATE_PAST,
            DateTimeFeatureActivationStrategy.of(
              ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault())
            )
          )
          .add(
            DATE_FUTURE,
            DateTimeFeatureActivationStrategy.of(
              ZonedDateTime.of(LocalDateTime.MAX, ZoneId.systemDefault())
            )
          )
          .add(DELEGATING, d -> delegateActive)
          .build()
      );
  }

  @Test
  public void itHasEnabledFeature() {
    assertThat(features.isActive(ALWAYS_ON, context)).isTrue();
  }

  @Test
  public void itDoesNotHaveDisabledFeature() {
    assertThat(features.isActive(ALWAYS_OFF, context)).isFalse();
  }

  @Test
  public void itHasPastEnabledFeature() {
    assertThat(features.isActive(DATE_PAST, context)).isTrue();
  }

  @Test
  public void itDoesNotHaveFutureEnabledFeature() {
    assertThat(features.isActive(DATE_FUTURE, context)).isFalse();
  }

  @Test
  public void itUsesDelegate() {
    delegateActive = false;
    assertThat(features.isActive(DELEGATING, context)).isEqualTo(delegateActive);
    delegateActive = true;
    assertThat(features.isActive(DELEGATING, context)).isEqualTo(delegateActive);
  }

  @Test
  public void itDefaultsToFalse() {
    assertThat(features.isActive("unknown", context)).isFalse();
  }
}
