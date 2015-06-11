package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TrimFilterTest {

  JinjavaInterpreter interpreter;
  TrimFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new TrimFilter();
  }

  @Test
  public void testTrim() {
    assertThat(filter.filter(" foo  ", interpreter)).isEqualTo("foo");
  }

}
