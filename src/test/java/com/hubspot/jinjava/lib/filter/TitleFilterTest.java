package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;


public class TitleFilterTest {

  JinjavaInterpreter interpreter;
  
  @Test
  public void testTitleCase() {
    assertThat(new TitleFilter().filter("this is string", interpreter)).isEqualTo("This Is String");
  }
  
}
