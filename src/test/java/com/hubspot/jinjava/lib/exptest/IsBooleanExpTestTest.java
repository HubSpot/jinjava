package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class IsBooleanExpTestTest {
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testIsBoolean() {
    assertThat(jinjava.render("{{ 1 is boolean }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 'true' is boolean }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ true is boolean }}", new HashMap<>()))
      .isEqualTo("true");
  }
}
