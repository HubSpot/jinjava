package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class EscapeJinjavaFilterTest {

  JinjavaInterpreter interpreter;
  EscapeJinjavaFilter f;

  @Before
  public void setup() {
    interpreter = mock(JinjavaInterpreter.class);
    f = new EscapeJinjavaFilter();
  }

  @Test
  public void testEscape() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter("{{ me & you }}", interpreter)).isEqualTo("&lbrace;&lbrace; me & you &rbrace;&rbrace;");
  }

}
