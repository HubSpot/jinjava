package com.hubspot.jinjava.util;

import static com.hubspot.jinjava.util.WhitespaceUtils.endsWith;
import static com.hubspot.jinjava.util.WhitespaceUtils.startsWith;
import static com.hubspot.jinjava.util.WhitespaceUtils.unwrap;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WhitespaceUtilsTest {

  @Test
  public void testStartsWith() {
    assertThat(startsWith("{foo}", "{")).isTrue();
    assertThat(startsWith(" {foo}", "{")).isTrue();
    assertThat(startsWith("  {foo}", "{foo}")).isTrue();
    assertThat(startsWith("foo}", "{")).isFalse();
    assertThat(startsWith(" foo}", "{")).isFalse();
  }

  @Test
  public void testEndsWith() {
    assertThat(endsWith("foo}", "}")).isTrue();
    assertThat(endsWith("foo} ", "}")).isTrue();
    assertThat(endsWith("foo} ", "foo}")).isTrue();
    assertThat(endsWith("foo", "}")).isFalse();
    assertThat(endsWith("foo ", "}")).isFalse();
  }

  @Test
  public void testUnwrap() {
    assertThat(unwrap("'foobar'", "'", "'")).isEqualTo("foobar");
    assertThat(unwrap(" - foobar", "-", "")).isEqualTo(" foobar");
    assertThat(unwrap("  'foobar' ", "'", "'")).isEqualTo("foobar");
    assertThat(unwrap("\t  <b>foobar</b>\n", "<b>", "</b>")).isEqualTo("foobar");
  }

}
