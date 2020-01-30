package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class EscapeFilterTest {

  JinjavaInterpreter interpreter;
  EscapeFilter f;

  @Before
  public void setup() {
    interpreter = mock(JinjavaInterpreter.class);
    f = new EscapeFilter();
  }

  @Test
  public void testEscape() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter("me & you", interpreter)).isEqualTo("me &amp; you");
    assertThat(f.filter("jared's & ted's bogus journey", interpreter)).isEqualTo("jared&#39;s &amp; ted&#39;s bogus journey");
    assertThat(f.filter(1, interpreter)).isEqualTo("1");
  }

}
