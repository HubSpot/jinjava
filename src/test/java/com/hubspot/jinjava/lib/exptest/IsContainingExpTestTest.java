package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class IsContainingExpTestTest extends BaseJinjavaTest {

  private static final String CONTAINING_TEMPLATE =
    "{%% if %s is containing %s %%}pass{%% else %%}fail{%% endif %%}";

  @Test
  public void itPassesOnContainedValue() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "2"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }

  @Test
  public void itFailsOnNullContainedValue() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, null]", "null"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnMissingValue() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "4"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnEmptyValue() {
    assertThat(
      jinjava.render(String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", ""), new HashMap<>())
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullValue() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "null"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullSequence() {
    assertThat(
      jinjava.render(String.format(CONTAINING_TEMPLATE, "null", "2"), new HashMap<>())
    )
      .isEqualTo("fail");
  }

  @Test
  public void itPerformsTypeConversion() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "'2'"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }
}
