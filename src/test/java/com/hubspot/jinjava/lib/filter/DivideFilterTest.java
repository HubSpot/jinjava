package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
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
  public void itThrowsAnExceptionOnDivideByZero() {
    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(
        () -> {
          jinjava.render(
            "{{ numerator|divide(denominator) }}",
            ImmutableMap.of("numerator", 10, "denominator", 0)
          );
        }
      );
  }
}
