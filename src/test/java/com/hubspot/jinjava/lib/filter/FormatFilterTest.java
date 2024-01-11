package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import java.util.HashMap;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormatFilterTest extends BaseJinjavaTest {

  @BeforeClass
  public static void beforeClass() {
    Locale.setDefault(Locale.ENGLISH);
  }

  @Test
  public void testFormatFilter() {
    assertThat(
      jinjava.render("{{ '%s - %s'|format(\"Hello?\", \"Foo!\") }}", new HashMap<>())
    )
      .isEqualTo("Hello? - Foo!");
  }

  @Test
  public void testFormatNumber() {
    assertThat(jinjava.render("{{ '%,d'|format(10000) }}", new HashMap<>()))
      .isEqualTo("10,000");
  }

  @Test
  public void itThrowsExceptionOnMissingFormatArgument() {
    assertThatThrownBy(() ->
        jinjava.render("{{ '%s %s'|format(10000) }}", new HashMap<>())
      )
      .isInstanceOf(FatalTemplateErrorsException.class)
      .hasMessageContaining("Missing format argument");
  }

  @Test
  public void itThrowsExceptionOnBadConversion() {
    assertThatThrownBy(() -> jinjava.render("{{ '%d'|format('hi') }}", new HashMap<>()))
      .isInstanceOf(FatalTemplateErrorsException.class)
      .hasMessageContaining("is not a compatible type");
  }

  @Test
  public void itThrowsExceptionOnFormat() {
    assertThatThrownBy(() -> jinjava.render("{{ '%0.0f'|format(1000) }}", new HashMap<>())
      )
      .isInstanceOf(FatalTemplateErrorsException.class)
      .hasMessageContaining("'%0.0f' is missing a width");
  }
}
