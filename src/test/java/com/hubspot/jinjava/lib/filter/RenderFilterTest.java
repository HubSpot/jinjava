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
}
