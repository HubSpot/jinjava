package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class NegatedExpTestTest extends BaseJinjavaTest {

  private static final String TEMPLATE =
    "{%% if %s is %s %s %%}pass{%% else %%}fail{%% endif %%}";
  private static final String CONTAINING_TEMPLATE =
    "{%% if %s is not containing %s %%}pass{%% else %%}fail{%% endif %%}";

  @Test
  public void itNegatesDefined() {
    assertThat(
      jinjava.render(String.format(TEMPLATE, "blah", "", "defined"), new HashMap<>())
    )
      .isEqualTo("fail");
    assertThat(
      jinjava.render(String.format(TEMPLATE, "blah", "not", "defined"), new HashMap<>())
    )
      .isEqualTo("pass");
  }

  @Test
  public void itNegatesContaining() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "4"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }
}
