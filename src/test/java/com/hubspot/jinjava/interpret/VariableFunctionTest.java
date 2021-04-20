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
package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class VariableFunctionTest {
  private static final DynamicVariableResolver VARIABLE_FUNCTION = s -> {
    switch (s) {
      case "name":
        return "Jared";
      case "title":
        return "Mr.";
      case "surname":
        return "Stehler";
      default:
        return null;
    }
  };

  @Test
  public void willUseTheFunctionToPopulateVariables() {
    final Jinjava jinjava = new Jinjava();
    jinjava.getGlobalContext().setDynamicVariableResolver(VARIABLE_FUNCTION);
    final Map<String, Object> context = new HashMap<>();

    final String template = "<div>Hello, {{ title }} {{ name }} {{ surname }}!</div>";

    final String renderedTemplate = jinjava.render(template, context);

    assertThat(renderedTemplate).isEqualTo("<div>Hello, Mr. Jared Stehler!</div>");
  }

  @Test
  public void willPreferTheContextOverTheFunctionToPopulateVariables() {
    final Jinjava jinjava = new Jinjava();
    jinjava.getGlobalContext().setDynamicVariableResolver(VARIABLE_FUNCTION);
    final Map<String, Object> context = new HashMap<>();
    context.put("name", "Greg");

    final String template = "<div>Hello, {{ title }} {{ name }} {{ surname }}!</div>";

    final String renderedTemplate = jinjava.render(template, context);

    assertThat(renderedTemplate).isEqualTo("<div>Hello, Mr. Greg Stehler!</div>");
  }
}
