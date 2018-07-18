package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class IsEqualToExpTestTest {

  private static final String EQUAL_TEMPLATE = "{{ %s is equalto %s }}";

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itEquatesNumbers() {
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "4"), new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "5"), new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void itEquatesStrings() {
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "\"jinjava\"", "\"jinjava\""), new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "\"jinjava\"", "\"not jinjava\""), new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void itEquatesBooleans() {
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "true", "true"), new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "true", "false"), new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void itEquatesDifferentTypes() {
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "\"4\""), new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "\"5\""), new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "'c'", "\"c\""), new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "'c'", "\"b\""), new HashMap<>())).isEqualTo("false");
  }
}
