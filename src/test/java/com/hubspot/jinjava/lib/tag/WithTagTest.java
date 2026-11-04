/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import java.util.HashMap;
import org.junit.Test;

public class WithTagTest extends BaseInterpretingTest {

  @Test
  public void itAssignsVariablesForTheBlockOnly() {
    String template =
      "{% with a = 1, b = 'x' %}{{ a }}{{ b }}{% endwith %}[{{ a }}{{ b }}]";
    assertThat(interpreter.render(template)).isEqualTo("1x[]");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itSupportsTheIssueExample() {
    String template =
      "{% macro cron(name, schedule, deadline) %}{{ name }}|{{ schedule }}|{{ deadline }}{% endmacro %}" +
      "{% with jobname=\"my_job\", schedule=\"0 1 * * 0\", deadline=300 %}" +
      "{{ cron(jobname, schedule, deadline) }}" +
      "{% endwith %}";
    assertThat(interpreter.render(template)).isEqualTo("my_job|0 1 * * 0|300");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itShadowsAndRestoresOuterVariables() {
    String template = "{% set x = 1 %}{% with x = 2 %}{{ x }}{% endwith %}{{ x }}";
    assertThat(interpreter.render(template)).isEqualTo("21");
  }

  @Test
  public void itEvaluatesAllValuesInTheOuterScope() {
    // Matches Jinja: b sees the outer a, not the a assigned by the same tag
    String template =
      "{% set a = 'outer' %}{% with a = 'inner', b = a %}{{ a }} {{ b }}{% endwith %} {{ a }}";
    assertThat(interpreter.render(template)).isEqualTo("inner outer outer");
  }

  @Test
  public void itScopesSetTagsInsideTheBody() {
    String template =
      "{% set kept = 'outer' %}{% with %}{% set foo = 42 %}{% set kept = 'inner' %}{{ foo }} {{ kept }}{% endwith %} [{{ foo }}] {{ kept }}";
    assertThat(interpreter.render(template)).isEqualTo("42 inner [] outer");
  }

  @Test
  public void itHandlesCommasAndOperatorsInsideValues() {
    String template =
      "{% with items = [1, 2, 3], same = (1 == 1), not_same = 1 != 1, small = 1 <= 2, " +
      "m = {'k': 'a,b'}, s = \"x=y, z\", f = 'a,b'|replace(',', ';') %}" +
      "{{ items|length }} {{ same }} {{ not_same }} {{ small }} {{ m.k }} {{ s }} {{ f }}" +
      "{% endwith %}";
    assertThat(interpreter.render(template))
      .isEqualTo("3 true false true a,b x=y, z a;b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itSupportsNestedWithBlocks() {
    String template =
      "{% with a = 1 %}{% with b = a + 1 %}{{ a }}{{ b }}{% endwith %}{{ a }}[{{ b }}]{% endwith %}";
    assertThat(interpreter.render(template)).isEqualTo("121[]");
  }

  @Test
  public void itWorksWithLoopVariables() {
    String template =
      "{% for i in [1, 2] %}{% with j = i * 10 %}{{ j }},{% endwith %}{% endfor %}[{{ j }}]";
    assertThat(interpreter.render(template)).isEqualTo("10,20,[]");
  }

  @Test
  public void itThrowsSyntaxErrorForInvalidTarget() {
    assertThatThrownBy(() -> interpreter.render("{% with a.b = 1 %}{{ a }}{% endwith %}"))
      .isInstanceOf(TemplateSyntaxException.class)
      .hasMessageContaining("can not assign to 'a.b'");
  }

  @Test
  public void itThrowsSyntaxErrorForMissingAssignment() {
    assertThatThrownBy(() -> interpreter.render("{% with a %}{{ a }}{% endwith %}"))
      .isInstanceOf(TemplateSyntaxException.class)
      .hasMessageContaining("expects assignments");
  }

  @Test
  public void itThrowsSyntaxErrorForMissingValue() {
    assertThatThrownBy(() -> interpreter.render("{% with a = %}{{ a }}{% endwith %}"))
      .isInstanceOf(TemplateSyntaxException.class)
      .hasMessageContaining("requires a value for 'a'");
  }

  @Test
  public void itThrowsSyntaxErrorForDuplicateNames() {
    assertThatThrownBy(() ->
        interpreter.render("{% with a = 1, a = 2 %}{{ a }}{% endwith %}")
      )
      .isInstanceOf(TemplateSyntaxException.class)
      .hasMessageContaining("more than once");
  }

  @Test
  public void itReportsSyntaxErrorsWhenRenderingForResult() {
    RenderResult result = jinjava.renderForResult(
      "{% with 1 = 2 %}{% endwith %}",
      new HashMap<>()
    );
    assertThat(result.getErrors())
      .extracting(TemplateError::getReason)
      .containsExactly(ErrorReason.SYNTAX_ERROR);
  }
}
