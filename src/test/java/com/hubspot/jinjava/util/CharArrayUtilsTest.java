package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CharArrayUtilsTest {

  @Test
  public void testCharArrayRegionMatches() {
    char[] s = "hello {% raw %} world {% endraw %}".toCharArray();

    assertThat(CharArrayUtils.charArrayRegionMatches(s, 0, "raw")).isFalse();
    assertThat(CharArrayUtils.charArrayRegionMatches(s, 9, "raw")).isTrue();

    assertThat(CharArrayUtils.charArrayRegionMatches(s, 25, "endraw")).isTrue();
    assertThat(CharArrayUtils.charArrayRegionMatches(s, 29, "endraw")).isFalse();
  }

}
