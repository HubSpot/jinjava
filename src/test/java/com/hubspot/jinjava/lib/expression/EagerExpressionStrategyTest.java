package com.hubspot.jinjava.lib.expression;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNodeTest;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerExpressionStrategyTest extends ExpressionNodeTest {

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    JinjavaInterpreter.pushCurrent(interpreter);
    context.put("deferred", DeferredValue.instance());
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itPreservesRawTags() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withNestedInterpretationEnabled(false)
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      assertExpectedOutput(
        "{{ '{{ foo }}' }} {{ '{% something %}' }} {{ 'not needed' }}",
        "{% raw %}{{ foo }}{% endraw %} {% raw %}{% something %}{% endraw %} not needed"
      );
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itPreservesRawTagsNestedInterpretation() {
    assertExpectedOutput(
      "{{ '{{ 12345 }}' }} {{ '{% print 'bar' %}' }} {{ 'not needed' }}",
      "12345 bar not needed"
    );
  }

  @Test
  public void itPrependsMacro() {
    assertExpectedOutput(
      "{% macro foo(bar) %} {{ bar }} {% endmacro %}{{ foo(deferred) }}",
      "{% macro foo(bar) %} {{ bar }} {% endmacro %}{{ foo(deferred) }}"
    );
  }

  @Test
  public void itPrependsSet() {
    context.put("foo", new PyList(new ArrayList<>()));
    assertExpectedOutput(
      "{{ foo.append(deferred) }}",
      "{% set foo = [] %}{{ foo.append(deferred) }}"
    );
  }

  @Test
  public void itDoesConcatenation() {
    context.put("foo", "y'all");
    assertExpectedOutput(
      "{{ 'oh, ' ~ foo ~ foo ~ ' toaster' }}",
      "oh, y'ally'all toaster"
    );
  }

  @Test
  public void itHandlesQuotesLikeJinja() {
    // {{ 'a|\'|\\\'|\\\\\'|"|\"|\\"|\\\\"|a ' ~ " b|\"|\\\"|\\\\\"|'|\'|\\'|\\\\'|b" }}
    // --> a|'|\'|\\'|"|"|\"|\\"|a  b|"|\"|\\"|'|'|\'|\\'|b
    assertExpectedOutput(
      "{{ 'a|\\'|\\\\\\'|\\\\\\\\\\'|\"|\\\"|\\\\\"|\\\\\\\\\"|a ' " +
      "~ \" b|\\\"|\\\\\\\"|\\\\\\\\\\\"|'|\\'|\\\\'|\\\\\\\\'|b\" }}",
      "a|'|\\'|\\\\'|\"|\"|\\\"|\\\\\"|a  b|\"|\\\"|\\\\\"|'|'|\\'|\\\\'|b"
    );
  }

  private void assertExpectedOutput(String inputTemplate, String expectedOutput) {
    assertThat(interpreter.render(inputTemplate)).isEqualTo(expectedOutput);
  }
}
