package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class BooleanExpTestsTest extends BaseJinjavaTest {

  @Test
  public void testIsBoolean() {
    assertThat(jinjava.render("{{ 1 is boolean }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 'true' is boolean }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ true is boolean }}", new HashMap<>()))
      .isEqualTo("true");
  }

  @Test
  public void testBooleanExpTests() {
    assertThat(jinjava.render("{{ true is true }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ true is false }}", new HashMap<>())).isEqualTo("false");
  }
}
