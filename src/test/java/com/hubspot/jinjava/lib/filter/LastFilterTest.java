package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class LastFilterTest {

  LastFilter filter;

  @Before
  public void setup() {
    filter = new LastFilter();
  }

  @Test
  public void lastReturnsNullForEmptyList() {
    assertThat(filter.filter(new ArrayList<String>(), null)).isNull();
  }

  @Test
  public void lastForSingleItemList() {
    assertThat(filter.filter(new Object[] { 1 }, null)).isEqualTo(1);
  }

  @Test
  public void lastForSeq() {
    assertThat(filter.filter(Arrays.asList(1, 2, 3), null)).isEqualTo(3);
  }

}
