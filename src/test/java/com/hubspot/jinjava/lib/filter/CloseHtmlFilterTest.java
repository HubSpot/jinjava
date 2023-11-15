package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import org.junit.Before;
import org.junit.Test;

public class CloseHtmlFilterTest extends BaseInterpretingTest {
  CloseHtmlFilter f;

  @Before
  public void setup() {
    f = new CloseHtmlFilter();
  }

  @Test
  public void itClosesTags() {
    String openTags = "<p>Hello, world!";
    assertThat(f.filter(openTags, interpreter)).isEqualTo("<p>Hello, world!</p>");
  }

  @Test
  public void itIgnoresClosedTags() {
    String openTags = "<p>Hello, world!</p>";
    assertThat(f.filter(openTags, interpreter)).isEqualTo("<p>Hello, world!</p>");
  }

  @Test
  public void itClosesMultipleTags() {
    String openTags = "<h1><p>Hello, world!";
    assertThat(f.filter(openTags, interpreter))
      .isEqualTo("<h1><p>Hello, world!</p></h1>");
  }
}
