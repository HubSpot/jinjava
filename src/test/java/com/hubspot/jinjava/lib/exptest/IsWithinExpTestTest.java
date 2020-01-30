package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class IsWithinExpTestTest {

  private static final String IN_TEMPLATE = "{%% if %s is within %s %%}pass{%% else %%}fail{%% endif %%}";

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itPassesOnValueInSequence() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "2", "[1, 2, 3]"), new HashMap<>())).isEqualTo("pass");
  }

  @Test
  public void itFailsOnNullValueInSequence() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "null", "[1, 2, null]"), new HashMap<>())).isEqualTo("fail");
  }

  @Test
  public void itFailsOnValueNotInSequence() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "4", "[1, 2, 3]"), new HashMap<>())).isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullValueNotInSequence() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "null", "[1, 2, 3]"), new HashMap<>())).isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullSequence() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "2", "null"), new HashMap<>())).isEqualTo("fail");
  }

  @Test
  public void itPerformsTypeConversion() {
    assertThat(jinjava.render(String.format(IN_TEMPLATE, "'1'", "[100000000000, 1]"), new HashMap<>())).isEqualTo("pass");
  }
}
