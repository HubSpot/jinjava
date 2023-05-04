package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.lib.filter.time.DateTimeFormatHelper.FIXED_DATE_TIME_FILTER_NULL_ARG;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.DateTimeFeatureActivationStrategy;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UnixTimestampFilterTest extends BaseInterpretingTest {
  private final ZonedDateTime d = ZonedDateTime.parse(
    "2013-11-06T14:22:00.000+00:00[UTC]"
  );
  private final String timestamp = Long.toString(d.toEpochSecond() * 1000);

  @Before
  public void setup() {
    interpreter.getContext().put("d", d);
  }

  @After
  public void tearDown() {
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itRendersFromDate() {
    assertThat(interpreter.renderFlat("{{ d|unixtimestamp }}")).isEqualTo(timestamp);
  }

  @Test
  public void itDefaultsToCurrentDate() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withDateTimeProvider(() -> d.toEpochSecond() * 1000)
        .withFeatureConfig(
          FeatureConfig
            .newBuilder()
            .add(FIXED_DATE_TIME_FILTER_NULL_ARG, DateTimeFeatureActivationStrategy.of(d))
            .build()
        )
        .build()
    );

    JinjavaInterpreter.pushCurrent(jinjava.newInterpreter());

    try {
      assertThat(jinjava.render("{{ null | unixtimestamp }}", ImmutableMap.of()))
        .isEqualTo(timestamp);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itDefaultsToDeprecationDate() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withDateTimeProvider(() -> d.toEpochSecond() * 1000)
        .withFeatureConfig(
          FeatureConfig
            .newBuilder()
            .add(FIXED_DATE_TIME_FILTER_NULL_ARG, DateTimeFeatureActivationStrategy.of(d))
            .build()
        )
        .build()
    );

    JinjavaInterpreter.pushCurrent(jinjava.newInterpreter());

    try {
      assertThat(jinjava.render("{{ null | unixtimestamp }}", ImmutableMap.of()))
        .isEqualTo("1383747720000");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
