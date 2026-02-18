package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import org.junit.Test;

public class DivideFilterTest extends BaseJinjavaTest {

  @Test
  public void itDivides() {
    assertThat(
      jinjava.render(
        "{{ numerator|divide(denominator) }}",
        ImmutableMap.of("numerator", 10, "denominator", 2)
      )
    )
      .isEqualTo("5");
    assertThat(
      jinjava.render(
        "{{ numerator // denominator }}",
        ImmutableMap.of("numerator", 10, "denominator", 2)
      )
    )
      .isEqualTo("5");
    assertThat(
      jinjava.render(
        "{{ numerator / denominator }}",
        ImmutableMap.of("numerator", 10, "denominator", 2)
      )
    )
      .isEqualTo("5.0");
  }

  @Test
  public void itDividesIntegersWithNonIntegerResult() {
    assertThat(
      jinjava.render(
        "{{ numerator|divide(denominator) }} {{ numerator / denominator }}",
        ImmutableMap.of("numerator", 9, "denominator", 10)
      )
    )
      .isEqualTo("1 0.9");
  }

  @Test
  public void itRendersWithMorePrecisionWithConfigOption() {
    Jinjava customJinjava = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
        )
        .withEnablePreciseDivideFilter(true)
        .build()
    );

    assertThat(
      jinjava.render(
        "{{ numerator|divide(denominator) }}",
        ImmutableMap.of("numerator", 2, "denominator", 100)
      )
    )
      .isEqualTo("0");

    assertThat(
      customJinjava.render(
        "{{ numerator|divide(denominator) }}",
        ImmutableMap.of("numerator", 2, "denominator", 100)
      )
    )
      .isEqualTo("0.02");
  }
}
