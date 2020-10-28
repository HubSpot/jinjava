package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class TrimFilterTest extends BaseInterpretingTest {
  TrimFilter filter;

  @Before
  public void setup() {
    filter = new TrimFilter();
  }

  @Test
  public void testTrim() {
    assertThat(filter.filter(" foo  ", interpreter)).isEqualTo("foo");
  }

  @Test
  public void testTrimSafeString() {
    assertThat(filter.filter(new SafeString(" foo  "), interpreter).toString())
      .isEqualTo("foo");
    assertThat(filter.filter(new SafeString(" foo  "), interpreter))
      .isInstanceOf(SafeString.class);
  }

  @Test
  public void itTrimsObject() {
    assertThat(filter.filter(new TestObject(), interpreter)).isEqualTo("hello");
  }

  private class TestObject {

    @Override
    public String toString() {
      return "    hello   ";
    }
  }
}
