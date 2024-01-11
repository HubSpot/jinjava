package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class IsFloatExpTestTest extends BaseJinjavaTest {

  @Test
  public void testValidFloats() {
    assertThat(jinjava.render("{{ 4.1 is float }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 0.0 is float }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4e4 is float }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4e-30 is float }}", new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void testInvalidFloats() {
    assertThat(jinjava.render("{{ 4 is float }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ -1 is float }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 0 is float }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 'four point oh' is float }}", new HashMap<>()))
      .isEqualTo("false");
  }

  @Test
  public void testWithAddFilter() {
    assertThat(jinjava.render("{{ (4|add(4)) is float }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ (4|add(4.5)) is float }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ (4|add(-4.5)) is float }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(
      jinjava.render("{{ (4|add(4.0000000000000000000001)) is float }}", new HashMap<>())
    )
      .isEqualTo("true");
    assertThat(jinjava.render("{{ (4|add(40.0)) is float }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(
      jinjava.render("{{ (4|add(1000000000000000000)) is float }}", new HashMap<>())
    )
      .isEqualTo("false");
  }
}
