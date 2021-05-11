package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CapitalizeFilterTest {

  @Test
  public void itCapitalizesNormalValues() {
    assertThat(new CapitalizeFilter().filter("foo", null)).isEqualTo("Foo");
  }

  @Test
  public void itCapitalizesSentences() {
    assertThat(new CapitalizeFilter().filter("foo is the best", null))
      .isEqualTo("Foo is the best");
  }

  @Test
  public void itLowercasesUppercasedCharsInSentences() {
    assertThat(new CapitalizeFilter().filter("foo is the bEST", null))
      .isEqualTo("Foo is the best");
  }
}
