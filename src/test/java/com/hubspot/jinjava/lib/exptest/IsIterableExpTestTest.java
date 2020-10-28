package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class IsIterableExpTestTest extends BaseJinjavaTest {

  @Test
  public void testIsIterable() {
    assertThat(jinjava.render("{{ null is iterable }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is iterable }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ [4] is iterable }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ [4, 'four'] is iterable }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ 'this string' is iterable }}", new HashMap<>()))
      .isEqualTo("false");
  }
}
