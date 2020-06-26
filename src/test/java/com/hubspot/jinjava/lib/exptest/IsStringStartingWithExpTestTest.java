package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class IsStringStartingWithExpTestTest {
  private static final String STARTING_TEMPLATE = "{{ var is string_startingwith arg }}";

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itReturnsTrueForContainedString() {
    assertThat(
        jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", "testing", "arg", "tes"))
      )
      .isEqualTo("true");
    assertThat(
        jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", "testing", "arg", ""))
      )
      .isEqualTo("true");
    assertThat(
        jinjava.render(
          STARTING_TEMPLATE,
          ImmutableMap.of("var", "testing", "arg", "testing")
        )
      )
      .isEqualTo("true");
  }

  @Test
  public void itReturnsFalseForExcludedString() {
    assertThat(
        jinjava.render(
          STARTING_TEMPLATE,
          ImmutableMap.of("var", "testing", "arg", "esting")
        )
      )
      .isEqualTo("false");
  }

  @Test
  public void itReturnsFalseForNull() {
    assertThat(jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", "testing")))
      .isEqualTo("false");
  }

  @Test
  public void itWorksForSafeString() {
    assertThat(
        jinjava.render(
          STARTING_TEMPLATE,
          ImmutableMap.of("var", "testing", "arg", new SafeString("tes"))
        )
      )
      .isEqualTo("true");
  }
}
