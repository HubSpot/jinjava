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
package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.WithTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerWithTagTest extends WithTagTest {

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        BaseJinjavaTest
          .newConfigBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    context.registerTag(new EagerWithTag());
    context.put("deferred", DeferredValue.instance());
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itFullyResolvesWhenNothingIsDeferred() {
    String output = interpreter.render(
      "{% with a = 1, b = [a, 2] %}{{ b|join(',') }}{% endwith %}"
    );
    assertThat(output).isEqualTo(",2");
    assertThat(context.getDeferredTokens()).isEmpty();
  }

  @Test
  public void itPreservesDeferredAssignmentsInAChildScope() {
    String output = interpreter.render(
      "{% with a = deferred, b = 2 %}{{ a }} {{ b }}{% endwith %}[{{ a }}{{ b }}]"
    );
    assertThat(output).doesNotContain("with");
    assertThat(output).contains("__ignored__");
    assertThat(renderSecondPass(output, ImmutableMap.of("deferred", "D")))
      .isEqualTo("D 2[]");
  }

  @Test
  public void itResolvesAssignedValuesInsideDeferredBlocks() {
    String output = interpreter.render(
      "{% with a = 'x' %}{% if deferred %}{{ a }}{% endif %}{% endwith %}[{{ a }}]"
    );
    assertThat(renderSecondPass(output, ImmutableMap.of("deferred", true)))
      .isEqualTo("x[]");
    assertThat(renderSecondPass(output, ImmutableMap.of("deferred", false)))
      .isEqualTo("[]");
  }

  @Test
  public void itKeepsDeferredSetTagsInsideTheBodyScoped() {
    String output = interpreter.render(
      "{% set foo = 'outer' %}{% with %}{% set foo = deferred %}{{ foo }}{% endwith %} {{ foo }}"
    );
    assertThat(renderSecondPass(output, ImmutableMap.of("deferred", "inner")))
      .isEqualTo("inner outer");
  }

  @Test
  public void itEvaluatesDeferredValuesInTheOuterScope() {
    String output = interpreter.render(
      "{% set a = 'outer' %}{% with a = deferred, b = a %}{{ a }} {{ b }}{% endwith %}"
    );
    assertThat(renderSecondPass(output, ImmutableMap.of("deferred", "inner")))
      .isEqualTo("inner outer");
  }

  private String renderSecondPass(String template, Map<String, Object> bindings) {
    // Render the eager output the way it would be rendered later: with no current
    // interpreter, so nothing is inherited from the eager context.
    Deque<JinjavaInterpreter> pushed = new ArrayDeque<>();
    while (JinjavaInterpreter.getCurrent() != null) {
      pushed.push(JinjavaInterpreter.getCurrent());
      JinjavaInterpreter.popCurrent();
    }
    try {
      return new Jinjava(BaseJinjavaTest.newConfigBuilder().build())
        .render(template, bindings);
    } finally {
      while (!pushed.isEmpty()) {
        JinjavaInterpreter.pushCurrent(pushed.pop());
      }
    }
  }
}
