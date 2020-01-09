package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class SafeFilterTest {

  private static final String HTML = "<a>Link</a>";

  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    interpreter.getContext().setAutoEscape(true);
    interpreter.getContext().put("v", HTML);
  }

  @After
  public void tearDown() throws Exception {
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itDoesNotEscapeStringMarkedAsSafe() throws Exception {
    assertThat(interpreter.renderFlat("{{ v|safe }}")).isEqualTo(HTML);
  }

}
