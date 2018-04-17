package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class NegatedExpTestTest {

  private static final String TEMPLATE = "{%% if %s is not %s %%}pass{%% else %%}fail{%% endif %%}";

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itNegatesDefined() {
    assertThat(jinjava.render(String.format(TEMPLATE, "blah", "defined"), new HashMap<>())).isEqualTo("pass");
  }
}
