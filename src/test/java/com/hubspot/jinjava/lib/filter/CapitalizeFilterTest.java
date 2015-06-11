package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CapitalizeFilterTest {

  @Test
  public void testCapitalize() {
    assertThat(new CapitalizeFilter().filter("foo", null)).isEqualTo("Foo");
  }

}
