package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Before;
import org.junit.Test;

public class RenderFilterTest extends BaseInterpretingTest {
  private RenderFilter filter;

  @Before
  public void setup() {
    filter = new RenderFilter();
  }

  @Test
  public void itRendersObject() {
    String stringToRender = "{% if null %}Hello{% else %}world{% endif %}";

    assertThat(filter.filter(stringToRender, interpreter)).isEqualTo("world");
  }

  @Test
  public void itRendersObjectWithinLimit() {
    String stringToRender = "{% if null %}Hello{% else %}world{% endif %}";

    assertThat(filter.filter(stringToRender, interpreter, "5")).isEqualTo("world");
  }

  @Test
  public void itDoesNotRenderObjectOverLimit() {
    String stringToRender = "{% if null %}Hello{% else %}world{% endif %}";

    assertThat(filter.filter(stringToRender, interpreter, "4")).isEqualTo("");
  }

  @Test
  public void itRendersPartialObjectOverLimit() {
    String stringToRender = "Hello{% if null %}Hello{% else %}world{% endif %}";

    assertThat(filter.filter(stringToRender, interpreter, "7")).isEqualTo("Hello");
  }

  @Test
  public void itCountsHtmlTags() {
    String stringToRender = "<p>Hello</p>{% if null %}Hello{% else %}world{% endif %}";

    assertThat(filter.filter(stringToRender, interpreter, "15"))
      .isEqualTo("<p>Hello</p>");
  }
}
