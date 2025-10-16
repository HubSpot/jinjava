package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Test;

public class CycleTagTest extends BaseInterpretingTest {

  @Test
  public void itDefaultsNullToImage() {
    String template = "{% for item in [0,1] %}{% cycle {{item}} %}{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo("{{item}}{{item}}");
  }

  @Test
  public void itDefaultsMultipleNullToImage() {
    String template = "{% for item in [0,1] %}{% cycle {{foo}},{{bar}} %}{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo("{{foo}}{{bar}}");
  }

  @Test
  public void itDefaultsNullToImageUsingAs() {
    String template =
      "{% for item in [0,1] %}{% cycle {{item}} as var %}{% cycle var %}{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo("{{item}}{{item}}");
  }

  @Test
  public void itDefaultsMultipleNullToImageUsingAs() {
    String template =
      "{% for item in [0,1] %}{% cycle {{foo}},{{bar}} as var %}{% cycle var %}{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo("{{foo}}{{bar}}");
  }

  @Test
  public void itHandlesEscapedQuotes() {
    String template =
      "{% set class = \"class='foo bar'\" %}{% for item in [0,1] %}{% cycle 'a','class=\\'foo bar\\'' %}.{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo("a.class='foo bar'.");
  }
}
