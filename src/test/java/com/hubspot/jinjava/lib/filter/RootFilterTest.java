package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class RootFilterTest {

  JinjavaInterpreter interpreter;
  RootFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new RootFilter();
  }

  @Test
  public void itCalculatesSquareRoot() {
    assertThat(filter.filter(100, interpreter)).isEqualTo(10D);
    assertThat(filter.filter(22500, interpreter)).isEqualTo(150D);
  }

  @Test
  public void itCalculatesNthRoot() {
    assertThat(filter.filter(9765625d, interpreter, "5.0")).isEqualTo(25D);
  }
}
