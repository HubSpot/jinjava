package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TitleFilterTest {

  @Test
  public void testTitleCase() {
    assertThat(new TitleFilter().filter("this is string", null)).isEqualTo("This Is String");
  }

}
