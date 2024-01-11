package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.SafeString;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class UpperFilterTest extends BaseInterpretingTest {

  private UpperFilter filter;

  @Before
  public void setup() {
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
