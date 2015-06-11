package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class FirstFilterTest {

  FirstFilter filter;

  @Before
  public void setup() {
    filter = new FirstFilter();
  }

  @Test
  public void firstReturnsNullForEmptyList() {
    assertThat(filter.filter(new Object[] {}, null)).isNull();
  }

  @Test
  public void firstForSeq() {
    assertThat(filter.filter(Arrays.asList("foo", "bar"), null)).isEqualTo("foo");
  }

}
