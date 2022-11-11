package com.hubspot.jinjava.lib.filter.time;

import com.hubspot.jinjava.Jinjava;
import org.junit.Before;

public class FormatDateFilterTest {
  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(FormatDateFilter.class);
  }
}
