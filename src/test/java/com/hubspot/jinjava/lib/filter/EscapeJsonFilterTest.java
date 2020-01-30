package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;

public class EscapeJsonFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(EscapeJsonFilter.class);
  }

  @Test
  public void testHandlesUnicode() {
    Map<String, String> vars = ImmutableMap.of("string", "A" + "\"" + "\\" + "/");
    assertThat(jinjava.render("{{ string|escapejson }}", vars)).isEqualTo("A\\\"\\\\\\/");
  }

  @Test
  public void testHandlesNonPrintableCharacters() {
    byte[] bytes = {0x4D, 0x13, 0x34, 0x20, 0x8};
    Map<String, String> vars = ImmutableMap.of("string", new String(bytes));
    assertThat(jinjava.render("{{ string|escapejson }}", vars)).isEqualTo("M\\u00134 \\b");
  }

  @Test
  public void testHandlesWhitespace() {
    assertThat(jinjava.render("{{ 'Testing\nlinebreak\n'|escapejson }}", new HashMap<>())).isEqualTo("Testing\\nlinebreak\\n");
    assertThat(jinjava.render("{{ 'Testing\ttabbing\t'|escapejson }}", new HashMap<>())).isEqualTo("Testing\\ttabbing\\t");
  }

  @Test
  public void testHandlesDoubleQuotes() {
    assertThat(jinjava.render("{{ 'Testing a \"quote for the week\"'|escapejson }}", new HashMap<>())).isEqualTo("Testing a \\\"quote for the week\\\"");
  }

  @Test
  public void testHandleSingleQuote() {
    Map<String, String> vars = ImmutableMap.of("string", "Testing a 'single quote' for the week");
    assertThat(jinjava.render("{{ string|escapejson }}", vars)).isEqualTo("Testing a 'single quote' for the week");
  }
}
