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
package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import org.junit.Before;
import org.junit.Test;

public class EagerAstBracketTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withExecutionMode(EagerExecutionMode.instance())
      .withNestedInterpretationEnabled(true)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      new Context(),
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);
    interpreter.getContext().put("word", "hello");
    interpreter.getContext().put("deferred", DeferredValue.instance());
  }

  @Test
  public void itIndexesStringsFromTheContext() {
    assertThat(interpreter.render("{{ word[1] }}")).isEqualTo("e");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itIndexesStringLiteralsWithNegativeIndices() {
    assertThat(interpreter.render("{{ 'hello'[-1] }}")).isEqualTo("o");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersIndexingAnUnresolvedString() {
    String partiallyRendered = interpreter.render("{{ deferred[1] }}");
    assertThat(partiallyRendered).isEqualTo("{{ deferred[1] }}");

    interpreter.getContext().put("deferred", "hello");
    assertThat(interpreter.render(partiallyRendered)).isEqualTo("e");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersIndexingAStringWithAnUnresolvedIndex() {
    String partiallyRendered = interpreter.render("{{ word[deferred] }}");
    assertThat(partiallyRendered).isEqualTo("{{ 'hello'[deferred] }}");

    interpreter.getContext().put("deferred", -1);
    assertThat(interpreter.render(partiallyRendered)).isEqualTo("o");
    assertThat(interpreter.getErrors()).isEmpty();
  }
}
