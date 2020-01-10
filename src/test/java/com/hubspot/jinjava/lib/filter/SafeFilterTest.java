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
  }

  @After
  public void tearDown() throws Exception {
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itDoesNotEscapeStringMarkedAsSafe() throws Exception {
    interpreter.getContext().put("html", HTML);
    assertThat(interpreter.renderFlat("{{ html|safe }}")).isEqualTo(HTML);
  }

  @Test
  public void itPassesVarThroughIfNotInstanceOfString() throws Exception {
    interpreter.getContext().put("number", -3);
    assertThat(interpreter.renderFlat("{{ number|safe|abs }}")).isEqualTo("3");
  }
}
