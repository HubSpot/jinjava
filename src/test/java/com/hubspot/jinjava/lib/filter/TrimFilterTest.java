package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

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

  @Test
  public void testTrimSafeString() {
    assertThat(filter.filter(new SafeString(" foo  "), interpreter).toString()).isEqualTo("foo");
    assertThat(filter.filter(new SafeString(" foo  "), interpreter)).isInstanceOf(SafeString.class);
  }

}
