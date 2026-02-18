package com.hubspot.jinjava.lib.expression;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNodeTest;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class EagerExpressionStrategyTest extends ExpressionNodeTest {

  private Jinjava jinjava;

  @Before
  public void eagerSetup() throws Exception {
    jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
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
        new Context(),
        BaseJinjavaTest
          .newConfigBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    nestedInterpreter =
      new JinjavaInterpreter(
        jinjava,
        interpreter.getContext(),
        BaseJinjavaTest
          .newConfigBuilder()
          .withNestedInterpretationEnabled(true)
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    interpreter.getContext().put("deferred", DeferredValue.instance());
    nestedInterpreter.getContext().put("deferred", DeferredValue.instance());
  }

  @Test
  public void itPreservesRawTags() {
    assertExpectedOutput(
      interpreter,
      "{{ '{{ foo }}' }} {{ '{% something %}' }} {{ 'not needed' }}",
      "{% raw %}{{ foo }}{% endraw %} {% raw %}{% something %}{% endraw %} not needed"
    );
  }

  @Test
  public void itPreservesRawTagsNestedInterpretation() {
    nestedInterpreter.getContext().put("bar", "bar");
    assertExpectedOutput(
      nestedInterpreter,
      "{{ '{{ 12345 }}' }} {{ '{% print bar %}' }} {{ 'not needed' }}",
      "12345 bar not needed"
    );
  }

  @Test
  public void itPrependsMacro() {
    assertExpectedOutput(
      interpreter,
      "{% macro foo(bar) %} {{ bar }} {% endmacro %}{{ foo(deferred) }}",
      "{% macro foo(bar) %} {{ bar }} {% endmacro %}{{ foo(deferred) }}"
    );
  }

  @Test
  public void itPrependsSet() {
    interpreter.getContext().put("foo", new PyList(new ArrayList<>()));
    assertExpectedOutput(
      interpreter,
      "{{ foo.append(deferred) }}",
      "{% set foo = [] %}{{ foo.append(deferred) }}"
    );
  }

  @Test
  public void itDoesConcatenation() {
    interpreter.getContext().put("foo", "y'all");
    assertExpectedOutput(
      interpreter,
      "{{ 'oh, ' ~ foo ~ foo ~ ' toaster' }}",
      "oh, y'ally'all toaster"
    );
  }

  @Test
  public void itHandlesQuotesLikeJinja() {
    // {{ 'a|\'|\\\'|\\\\\'|"|\"|\\"|\\\\"|a ' ~ " b|\"|\\\"|\\\\\"|'|\'|\\'|\\\\'|b" }}
    // --> a|'|\'|\\'|"|"|\"|\\"|a  b|"|\"|\\"|'|'|\'|\\'|b
    assertExpectedOutput(
      interpreter,
      "{{ 'a|\\'|\\\\\\'|\\\\\\\\\\'|\"|\\\"|\\\\\"|\\\\\\\\\"|a ' " +
      "~ \" b|\\\"|\\\\\\\"|\\\\\\\\\\\"|'|\\'|\\\\'|\\\\\\\\'|b\" }}",
      "a|'|\\'|\\\\'|\"|\"|\\\"|\\\\\"|a  b|\"|\\\"|\\\\\"|'|'|\\'|\\\\'|b"
    );
  }

  @Test
  public void itGoesIntoDeferredExecutionMode() {
    assertExpectedOutput(
      interpreter,
      "{{ is_deferred_execution_mode() }}" +
      "{% if deferred %}{{ is_deferred_execution_mode() }}{% endif %}" +
      "{{ is_deferred_execution_mode() }}",
      "false{% if deferred %}true{% endif %}false"
    );
  }

  @Test
  public void itGoesIntoDeferredExecutionModeWithMacro() {
    assertExpectedOutput(
      interpreter,
      "{% macro def() %}{{ is_deferred_execution_mode() }}{% endmacro %}" +
      "{{ def() }}" +
      "{% if deferred %}{{ def() }}{% endif %}" +
      "{{ def() }}",
      "false{% if deferred %}true{% endif %}false"
    );
  }

  @Test
  public void itDoesNotGoIntoDeferredExecutionModeUnnecessarily() {
    assertExpectedOutput(interpreter, "{{ is_deferred_execution_mode() }}", "false");
    interpreter.getContext().setDeferredExecutionMode(true);
    assertExpectedOutput(interpreter, "{{ is_deferred_execution_mode() }}", "true");
  }

  @Test
  public void itDoesNotNestedInterpretIfThereAreFakeNotes() {
    assertExpectedOutput(
      interpreter,
      "{{ '{#something_to_{{keep}}' }}",
      "{#something_to_{{keep}}"
    );
  }

  @Test
  public void itDoesNotReconstructWithDoubleCurlyBraces() {
    interpreter.getContext().put("foo", ImmutableMap.of("foo", ImmutableMap.of()));
    assertExpectedOutput(
      interpreter,
      "{{ deferred ~ foo }}",
      "{{ deferred ~ {'foo': {} } }}"
    );
  }

  @Test
  public void itDoesNotReconstructWithNestedDoubleCurlyBraces() {
    interpreter
      .getContext()
      .put("foo", ImmutableMap.of("foo", ImmutableMap.of("bar", ImmutableMap.of())));
    assertExpectedOutput(
      interpreter,
      "{{ deferred ~ foo }}",
      "{{ deferred ~ {'foo': {'bar': {} } } }}"
    );
  }

  @Test
  public void itDoesNotReconstructDirectlyWrittenWithDoubleCurlyBraces() {
    assertExpectedOutput(
      interpreter,
      "{{ deferred ~ {\n'foo': {\n'bar': deferred\n}\n}\n }}",
      "{{ deferred ~ {'foo': {'bar': deferred} } }}"
    );
  }

  @Test
  public void itReconstructsWithNestedInterpretation() {
    interpreter.getContext().put("foo", "{{ print 'bar' }}");
    assertExpectedOutput(
      interpreter,
      "{{ deferred ~ foo }}",
      "{{ deferred ~ '{{ print \\'bar\\' }}' }}"
    );
  }

  @Test
  public void itDoesNotDoNestedInterpretationWithSyntaxErrors() {
    try (
      InterpreterScopeClosable c = interpreter.enterScope(
        ImmutableMap.of(Library.TAG, ImmutableSet.of("print"))
      )
    ) {
      interpreter.getContext().put("foo", "{% print 'bar' %}");
      // Rather than rendering this to an empty string
      assertExpectedOutput(interpreter, "{{ foo }}", "{% print 'bar' %}");
    }
  }

  private void assertExpectedOutput(
    JinjavaInterpreter interpreter,
    String inputTemplate,
    String expectedOutput
  ) {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      assertThat(a.value().render(inputTemplate)).isEqualTo(expectedOutput);
    }
  }

  public static boolean isDeferredExecutionMode() {
    return JinjavaInterpreter.getCurrent().getContext().isDeferredExecutionMode();
  }
}
