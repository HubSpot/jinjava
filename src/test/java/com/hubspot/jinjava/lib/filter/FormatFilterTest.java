package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class FormatFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testFormatFilter() {
    assertThat(jinjava.render("{{ '%s - %s'|format(\"Hello?\", \"Foo!\") }}", new HashMap<String, Object>())).isEqualTo("Hello? - Foo!");
  }

}
