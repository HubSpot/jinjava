package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Test;

public class IsUpperExpTestTest extends BaseJinjavaTest {

  private static final String STARTING_TEMPLATE = "{{ var is upper }}";
  private static final String SAFE_TEMPLATE = "{{ (var|safe) is upper }}";

  @Test
  public void itReturnsTrueForUpperString() {
    assertThat(jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", "UPPER")))
      .isEqualTo("true");
  }

  @Test
  public void itReturnsFalseForLowerString() {
    assertThat(jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", "lower")))
      .isEqualTo("false");
  }

  @Test
  public void itWorksForSafeStrings() {
    assertThat(
      jinjava.render(STARTING_TEMPLATE, ImmutableMap.of("var", new SafeString("UPPER")))
    )
      .isEqualTo("true");
    assertThat(jinjava.render(SAFE_TEMPLATE, ImmutableMap.of("var", "UPPER")))
      .isEqualTo("true");
  }
}
