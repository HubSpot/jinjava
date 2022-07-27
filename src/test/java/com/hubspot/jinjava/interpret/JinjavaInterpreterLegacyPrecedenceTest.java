package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class JinjavaInterpreterLegacyPrecedenceTest {
  Jinjava legacy;
  Jinjava modern;

  @Before
  public void setUp() throws Exception {
    legacy =
      new Jinjava(
        JinjavaConfig.newBuilder().withLegacyOverrides(LegacyOverrides.NONE).build()
      );
    modern =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withLegacyOverrides(LegacyOverrides.newBuilder().build())
          .build()
      );
  }

  @Test
  public void itBindsUnaryMinusTighterThanCmp() {
    String template = "{{ (-5 > 4) }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("false");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void itBindsUnaryMinusTighterThanIs() {
    String template = "{{ (-5 is integer) }}";

    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(() -> legacy.render(template, new HashMap<>()))
      .withMessage("Cannot negate 'class java.lang.Boolean'");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void itBindsUnaryMinusTighterThanIsNot() {
    String template = "{{ (-5 is not integer) }}";

    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(() -> legacy.render(template, new HashMap<>()))
      .withMessage("Cannot negate 'class java.lang.Boolean'");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void itBindsUnaryMinusTighterThanFilters() {
    String template = "{{ (-5 | abs) }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("-5");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("5");
  }

  @Test
  public void itBindsFiltersTighterThanMul() {
    String template = "{{ (-5 * -4 | abs) }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("20");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("-20");
  }

  @Test
  public void itInterpretsFilterChainsInOrder() {
    String template = "{{ 'foo' | upper | replace('O', 'A') }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("FAA");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("FAA");
  }
}
