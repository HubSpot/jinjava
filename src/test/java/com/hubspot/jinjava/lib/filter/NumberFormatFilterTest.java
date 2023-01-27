package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class NumberFormatFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {}

  @Test
  public void testNumberFormatFilter() {
    assertThat(
        jinjava.render("{{1000|format_number('en-US')}}", new HashMap<String, Object>())
      )
      .isEqualTo("1,000");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('en-US') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1,000.333");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('en-US', 2) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1,000.33");

    assertThat(
        jinjava.render("{{ 1000|format_number('fr') }}", new HashMap<String, Object>())
      )
      .isEqualTo("1\u00a0000");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('fr') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1\u00a0000,333");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('fr', 2) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1\u00a0000,33");

    assertThat(
        jinjava.render("{{ 1000|format_number('es') }}", new HashMap<String, Object>())
      )
      .isEqualTo("1.000");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('es') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1.000,333");
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('es', 2) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("1.000,33");
  }
}
