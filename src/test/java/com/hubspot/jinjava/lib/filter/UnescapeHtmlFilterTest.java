package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Before;
import org.junit.Test;

public class UnescapeHtmlFilterTest extends BaseInterpretingTest {

  UnescapeHtmlFilter f;

  @Before
  public void setup() {
    f = new UnescapeHtmlFilter();
  }

  @Test
  public void itUnescapes() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter("me &amp; you", interpreter)).isEqualTo("me & you");
    assertThat(f.filter("jeff&#39;s &amp; jack&#39;s bog&uuml;s journey", interpreter))
      .isEqualTo("jeff's & jack's bogüs journey");
    assertThat(f.filter(1, interpreter)).isEqualTo("1");
  }
}
