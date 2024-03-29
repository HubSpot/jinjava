package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class IsContainingAllExpTestTest extends BaseJinjavaTest {

  private static final String CONTAINING_TEMPLATE =
    "{%% if %s is containingall %s %%}pass{%% else %%}fail{%% endif %%}";

  @Test
  public void itPassesOnContainedValues() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2]"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }

  @Test
  public void itPassesOnContainedDuplicatedValues() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2, 2]"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }

  @Test
  public void itFailsOnOnlySomeContainedValues() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2, 4]"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullSequence() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "null", "[1, 2, 4]"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itFailsOnNullValues() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "null"),
        new HashMap<>()
      )
    )
      .isEqualTo("fail");
  }

  @Test
  public void itPerformsTypeConversion() {
    assertThat(
      jinjava.render(
        String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "['2', '3']"),
        new HashMap<>()
      )
    )
      .isEqualTo("pass");
  }
}
