package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class IsEscapedExpTestTest {
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testIsEscaped() {
    assertThat(jinjava.render("{{ 'test' is escaped }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ ('test'|escape) is escaped }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ ('test'|safe) is escaped }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ '' is escaped }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ null is escaped }}", new HashMap<>()))
      .isEqualTo("false");
  }
}
