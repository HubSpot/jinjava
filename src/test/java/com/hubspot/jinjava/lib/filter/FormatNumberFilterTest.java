package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class FormatNumberFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {}

  @Test
  public void testFormatNumberFilter() {
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
      .isEqualTo(
        String.format(
          "1%s000",
          DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator()
        )
      );
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('fr') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo(
        String.format(
          "1%s000,333",
          DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator()
        )
      );
    assertThat(
        jinjava.render(
          "{{ 1000.333|format_number('fr', 2) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo(
        String.format(
          "1%s000,33",
          DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator()
        )
      );

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
