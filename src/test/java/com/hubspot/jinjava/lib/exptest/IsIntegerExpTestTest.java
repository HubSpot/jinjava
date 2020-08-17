package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class IsIntegerExpTestTest {
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testValidIntegers() {
    assertThat(jinjava.render("{{ 4 is integer }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ -1 is integer }}", new HashMap<>())).isEqualTo("true");
    long number = Integer.MAX_VALUE;
    assertThat(
        jinjava.render(String.format("{{ %d is integer }}", number + 1), new HashMap<>())
      )
      .isEqualTo("true");
    assertThat(jinjava.render("{{ 1000000000000000000 is integer }}", new HashMap<>()))
      .isEqualTo("true");
  }

  @Test
  public void testInvalidIntegers() {
    assertThat(jinjava.render("{{ 'four' is integer }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ false is integer }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 4.1 is integer }}", new HashMap<>()))
      .isEqualTo("false");
  }

  @Test
  public void testWithAddFilter() {
    assertThat(jinjava.render("{{ (4|add(4)) is integer }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ (4|add(4.5)) is integer }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ (4|add(-4.5)) is integer }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(
        jinjava.render(
          "{{ (4|add(4.0000000000000000000001)) is integer }}",
          new HashMap<>()
        )
      )
      .isEqualTo("false");
    assertThat(jinjava.render("{{ (4|add(40.0)) is integer }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(
        jinjava.render("{{ (4|add(1000000000000000000)) is integer }}", new HashMap<>())
      )
      .isEqualTo("true");
  }
}
