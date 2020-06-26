package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class UpperFilterTest {
  private UpperFilter filter;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new UpperFilter();
  }

  @Test
  public void testUpper() {
    Assertions.assertThat(filter.filter("lower", interpreter)).isEqualTo("LOWER");
  }

  @Test
  public void testUpperSafeString() {
    Assertions
      .assertThat(filter.filter(new SafeString("lower"), interpreter).toString())
      .isEqualTo("LOWER");
    Assertions
      .assertThat(filter.filter(new SafeString("lower"), interpreter))
      .isInstanceOf(SafeString.class);
  }
}
