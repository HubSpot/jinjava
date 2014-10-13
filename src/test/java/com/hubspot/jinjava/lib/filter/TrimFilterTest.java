package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


public class TrimFilterTest {

  JinjavaInterpreter interpreter;
  TrimFilter filter;
  
  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();
    Context context = new Context();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    filter = new TrimFilter();
  }
  
  @Test
  public void testTrim() {
    assertThat(filter.filter(" foo  ", interpreter)).isEqualTo("foo");
  }
  
}
