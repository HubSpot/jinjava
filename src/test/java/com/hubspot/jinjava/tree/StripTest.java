package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Test;

public class StripTest extends BaseInterpretingTest {

  @Test
  public void itStrips() {
    String expression =
      "{% for i in range(10) -%}\r\n{% for j in range(10) -%}\r\n{% endfor %}";
    String render = interpreter.render(expression);
    assertThat(render).isEqualTo("");
  }
}
