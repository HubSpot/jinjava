package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import org.junit.Test;

public class ContinueTagTest extends BaseInterpretingTest {

  @Test
  public void testContinue() {
    String template =
      "{% for item in [1, 2, 3, 4] %}{% if item % 2 == 0 %}{% continue %}{% endif %}{{ item }}{% endfor %}";

    RenderResult rendered = jinjava.renderForResult(template, context);
    assertThat(rendered.getOutput()).isEqualTo("13");
  }

  @Test
  public void testNestedContinue() {
    String template =
      "{% for item in [1, 2, 3, 4] %}{% for item2 in [5, 6, 7] %}{% continue %}{{ item2 }}{% endfor %}{{ item }}{% endfor %}";

    RenderResult rendered = jinjava.renderForResult(template, context);
    assertThat(rendered.getOutput()).isEqualTo("1234");
  }

  @Test
  public void testContinueWithEarlierContent() {
    String template =
      "{% for item in [1, 2, 3, 4] %}{{ item }}{% if item % 2 == 0 %}{% continue %}{% endif %}{{ item }}{% endfor %}";

    RenderResult rendered = jinjava.renderForResult(template, context);
    assertThat(rendered.getOutput()).isEqualTo("112334");
  }

  @Test
  public void testContinueOutOfContext() {
    String template = "{% continue %}";

    RenderResult rendered = jinjava.renderForResult(template, context);
    assertThat(rendered.getOutput()).isEqualTo("");
    assertThat(rendered.getErrors()).hasSize(1);
    assertThat(rendered.getErrors().get(0).getSeverity())
      .isEqualTo(TemplateError.ErrorType.FATAL);
    assertThat(rendered.getErrors().get(0).getMessage())
      .contains("NotInLoopException: `continue` called while not in a for loop");
  }
}
