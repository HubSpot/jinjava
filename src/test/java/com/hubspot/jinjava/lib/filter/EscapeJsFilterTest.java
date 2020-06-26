package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class EscapeJsFilterTest {
  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void testHandlesUnicdoe() {
    Map<String, String> vars = ImmutableMap.of(
      "string",
      "A" + "\u00ea" + "\u00f1" + "\u00fc" + "C"
    );
    assertThat(jinjava.render("{{ string|escapejs }}", vars))
      .isEqualTo("A\\u00EA\\u00F1\\u00FCC");
  }

  @Test
  public void testHandlesNonPrintableCharacters() {
    byte[] bytes = { 0x4D, 0x13, 0x34, 0x20, 0x8 };
    Map<String, String> vars = ImmutableMap.of("string", new String(bytes));
    assertThat(jinjava.render("{{ string|escapejs }}", vars)).isEqualTo("M\\u00134 \\b");
  }

  @Test
  public void testHandlesWhitespace() {
    assertThat(jinjava.render("{{ 'Testing\nlinebreak\n'|escapejs }}", new HashMap<>()))
      .isEqualTo("Testing\\nlinebreak\\n");
    assertThat(jinjava.render("{{ 'Testing\ttabbing\t'|escapejs }}", new HashMap<>()))
      .isEqualTo("Testing\\ttabbing\\t");
  }

  @Test
  public void testHandlesDoubleQuotes() {
    assertThat(
        jinjava.render(
          "{{ 'Testing a \"quote for the week\"'|escapejs }}",
          new HashMap<>()
        )
      )
      .isEqualTo("Testing a \\\"quote for the week\\\"");
  }

  @Test
  public void testSafeStringCanBeEscaped() {
    assertThat(
        jinjava.render("{{ 'Testing\nlineb\"reak\n'|safe|escapejs }}", new HashMap<>())
      )
      .isEqualTo("Testing\\nlineb\\\"reak\\n");
  }
}
