package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class EscapeJinjavaFilterTest extends BaseInterpretingTest {
  EscapeJinjavaFilter f;

  @Before
  public void setup() {
    f = new EscapeJinjavaFilter();
  }

  @Test
  public void testEscape() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter("{{ me & you }}", interpreter))
      .isEqualTo("&lbrace;&lbrace; me & you &rbrace;&rbrace;");
  }

  @Test
  public void testSafeStringCanBeEscaped() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter(new SafeString("{{ me & you }}"), interpreter).toString())
      .isEqualTo("&lbrace;&lbrace; me & you &rbrace;&rbrace;");
    assertThat(f.filter(new SafeString("{{ me & you }}"), interpreter))
      .isInstanceOf(SafeString.class);
  }
}
