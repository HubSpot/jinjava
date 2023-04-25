package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import org.junit.Test;

public class DoTagTest extends BaseInterpretingTest {

  @Test
  public void itResolvesExpressions() {
    String template = "{% set output = [] %}{% do output.append('hey') %}{{ output }}";
    assertThat(jinjava.render(template, Maps.newHashMap())).isEqualTo("['hey']");
  }

  @Test
  public void itAddsTemplateErrorOnEmptyExpressionAndNoEndTag() {
    String template = "{% do %}";
    RenderResult renderResult = jinjava.renderForResult(template, Maps.newHashMap());
    assertThat(renderResult.getErrors()).hasSize(1);
    assertThat(renderResult.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void itEvaluatesDoBlockAndDiscardsResult() {
    String template =
      "{% do %}{% set foo = 1 %}{{ foo }}{% enddo %}{% if foo == 1 %}Yes{% endif %}";
    assertThat(jinjava.render(template, Maps.newHashMap())).isEqualTo("Yes");
  }
}
