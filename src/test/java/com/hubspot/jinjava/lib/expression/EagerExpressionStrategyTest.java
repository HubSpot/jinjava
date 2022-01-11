package com.hubspot.jinjava.lib.expression;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNodeTest;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerExpressionStrategyTest extends ExpressionNodeTest {

  @Before
  public void eagerSetup() throws Exception {
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "is_deferred_execution_mode",
          this.getClass().getDeclaredMethod("isDeferredExecutionMode")
        )
      );
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
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
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
    context.put("bar", "bar");
    assertExpectedOutput(
      "{{ '{{ 12345 }}' }} {{ '{% print bar %}' }} {{ 'not needed' }}",
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

  @Test
  public void itGoesIntoDeferredExecutionMode() {
    assertExpectedOutput(
      "{{ is_deferred_execution_mode() }}" +
      "{% if deferred %}{{ is_deferred_execution_mode() }}{% endif %}" +
      "{{ is_deferred_execution_mode() }}",
      "false{% if deferred %}true{% endif %}false"
    );
  }

  @Test
  public void itDoesNotGoIntoDeferredExecutionModeWithMacro() {
    assertExpectedOutput(
      "{% macro def() %}{{ is_deferred_execution_mode() }}{% endmacro %}" +
      "{{ def() }}" +
      "{% if deferred %}{{ def() }}{% endif %}" +
      "{{ def() }}",
      "false{% if deferred %}false{% endif %}false"
    );
  }

  @Test
  public void itDoesNotGoIntoDeferredExecutionModeUnnecessarily() {
    assertExpectedOutput("{{ is_deferred_execution_mode() }}", "false");
    interpreter.getContext().setDeferredExecutionMode(true);
    assertExpectedOutput("{{ is_deferred_execution_mode() }}", "true");
  }

  @Test
  public void itDoesNotNestedInterpretIfThereAreFakeNotes() {
    assertExpectedOutput("{{ '{#something_to_{{keep}}' }}", "{#something_to_{{keep}}");
  }

  @Test
  public void itDoesNotReconstructWithDoubleCurlyBraces() {
    interpreter.getContext().put("foo", ImmutableMap.of("foo", ImmutableMap.of()));
    assertExpectedOutput("{{ deferred ~ foo }}", "{{ deferred ~ {'foo': {} } }}");
  }

  @Test
  public void itDoesNotReconstructWithNestedDoubleCurlyBraces() {
    interpreter
      .getContext()
      .put("foo", ImmutableMap.of("foo", ImmutableMap.of("bar", ImmutableMap.of())));
    assertExpectedOutput(
      "{{ deferred ~ foo }}",
      "{{ deferred ~ {'foo': {'bar': {} } } }}"
    );
  }

  @Test
  public void itReconstructsWithNestedInterpretation() {
    interpreter.getContext().put("foo", "{{ print 'bar' }}");
    assertExpectedOutput(
      "{{ deferred ~ foo }}",
      "{{ deferred ~ '{{ print \\'bar\\' }}' }}"
    );
  }

  private void assertExpectedOutput(String inputTemplate, String expectedOutput) {
    assertThat(interpreter.render(inputTemplate)).isEqualTo(expectedOutput);
  }

  public static boolean isDeferredExecutionMode() {
    return JinjavaInterpreter.getCurrent().getContext().isDeferredExecutionMode();
  }
}
