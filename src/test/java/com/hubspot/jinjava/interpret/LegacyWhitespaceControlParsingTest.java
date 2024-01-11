package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class LegacyWhitespaceControlParsingTest {

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
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withParseWhitespaceControlStrictly(true).build()
          )
          .build()
      );
  }

  @Test
  public void itInterpretsStandaloneNegatives() {
    String template = "{{ -10 }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("10");
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("-10");
  }

  @Test
  public void itInterpretsStandaloneNegativesWithWhitespace() {
    String template = "{{ - 10 }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("10");

    // Somewhat surprising, but this aligns with Jinja
    assertThat(modern.render(template, new HashMap<>())).isEqualTo("-10");
  }

  @Test
  public void itErrorsOnTrailingDash() {
    String template = "{{ 10- }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("10");
    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(() -> modern.render(template, new HashMap<>()))
      .withMessageContaining("syntax error at position 6");
  }

  @Test
  public void itErrorsOnSpacedTrailingDash() {
    String template = "{{ 10 - }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("10");
    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(() -> modern.render(template, new HashMap<>()))
      .withMessageContaining("syntax error at position 7");
  }

  @Test
  public void itErrorsOnSpacedDashesOnBothSides() {
    String template = "{{ - 10 - }}";

    assertThat(legacy.render(template, new HashMap<>())).isEqualTo("10");
    assertThatExceptionOfType(FatalTemplateErrorsException.class)
      .isThrownBy(() -> modern.render(template, new HashMap<>()))
      .withMessageContaining("syntax error at position 9");
  }
}
