package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

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
    assertThat(f.filter("jared's & ted's bogus journey", interpreter))
      .isEqualTo("jared&#39;s &amp; ted&#39;s bogus journey");
    assertThat(f.filter(1, interpreter)).isEqualTo("1");
  }

  @Test
  public void testSafeStringCanBeEscaped() {
    assertThat(
        f
          .filter(new SafeString("<a>Previously marked as safe<a/>"), interpreter)
          .toString()
      )
      .isEqualTo("&lt;a&gt;Previously marked as safe&lt;a/&gt;");
    assertThat(f.filter(new SafeString("<a>Previously marked as safe<a/>"), interpreter))
      .isInstanceOf(SafeString.class);
  }
}
