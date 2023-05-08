package com.hubspot.jinjava.lib.fn.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerMacroFunctionTest extends BaseInterpretingTest {

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .withEnableRecursiveMacroCalls(true)
          .withMaxMacroRecursionDepth(10)
          .build()
      );
    context.put("deferred", DeferredValue.instance());
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itReconstructsImage() {
    String name = "foo";
    String code = "{% macro foo(bar) %}It's: {{ bar }}{% endmacro %}";
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(name, code);
    assertThat(eagerMacroFunction.reconstructImage(name)).isEqualTo(code);
  }

  @Test
  public void itResolvesFromContext() {
    context.put("foobar", "resolved");
    String name = "foo";
    String code = "{% macro foo(bar) %}{{ foobar }} and {{ bar }}{% endmacro %}";
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(name, code);
    assertThat(eagerMacroFunction.reconstructImage(name))
      .isEqualTo("{% macro foo(bar) %}resolved and {{ bar }}{% endmacro %}");
  }

  @Test
  public void itReconstructsForAliasedName() {
    context.remove("deferred");
    String name = "foo";
    String fullName = "local." + name;
    String codeFormat = "{%% macro %s(bar) %%}It's: {{ bar }}{%% endmacro %%}";
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(
      name,
      String.format(codeFormat, name)
    );
    assertThat(eagerMacroFunction.reconstructImage(fullName))
      .isEqualTo(String.format(codeFormat, fullName));
  }

  @Test
  public void itResolvesFromSet() {
    String template =
      "{% macro foo(foobar, other) %}" +
      " {% do foobar.update({'a': 'b'} ) %} " +
      " {{ foobar }}  and {{ other }}" +
      "{% endmacro %}" +
      "{% set bar = {}  %}" +
      "{% call foo(bar, deferred) %} {% endcall %}" +
      "{{ bar }}";
    String firstPass = interpreter.render(template);
    assertThat(firstPass).isEqualTo(template);
  }

  @Test
  public void itReconstructsImageWithNamedParams() {
    String name = "foo";
    String code = "{% macro foo(bar, baz=0) %}It's: {{ bar }}, {{ baz }}{% endmacro %}";
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(name, code);
    assertThat(eagerMacroFunction.reconstructImage(name)).isEqualTo(code);
  }

  @Test
  public void itPartiallyEvaluatesMacroFunction() {
    // Put this test here because it's only used in eager execution
    context.put("deferred", DeferredValue.instance());
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(
      "foo",
      "{% macro foo(bar) %}It's: {{ bar }}, {{ deferred }}{% endmacro %}"
    );
    assertThatThrownBy(() -> eagerMacroFunction.evaluate("Bar"))
      .isInstanceOf(DeferredValueException.class);
    try (TemporaryValueClosable<Boolean> ignored = context.withPartialMacroEvaluation()) {
      assertThat(eagerMacroFunction.evaluate("Bar"))
        .isEqualTo("It's: Bar, {{ deferred }}");
    }
  }

  @Test
  public void itDoesNotAllowStackOverflow() {
    String name = "rec";
    String code =
      "{% macro rec(num=0) %}{% if num > 0 %}{{ num }}-{{ rec(num - 1)}}{% endif %}{% endmacro %}";
    EagerMacroFunction eagerMacroFunction = makeMacroFunction(name, code);
    String output;
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      output = eagerMacroFunction.reconstructImage();
    }
    assertThat(interpreter.render(output + "{{ rec(5) }}")).isEqualTo("5-4-3-2-1-");
  }

  @Test
  public void itDefersDifferentMacrosWithSameName() {
    // This kind of situation can happen when importing and the imported macro function calls another that has a name clash
    String foo1Code = "{% macro foo(var) %}This is the {{ var }}{% endmacro %}";
    String barCode = "{% macro bar(var) %}^{{ foo(var) }}^{% endmacro %}";
    String foo2Code = "{% macro foo(var) %}~{{ bar(var) }}~{% endmacro %}";
    MacroFunction foo1Macro;
    MacroFunction barMacro;
    try (InterpreterScopeClosable c = interpreter.enterScope()) { // Imitate importing
      interpreter.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, "some_path");
      foo1Macro = makeMacroFunction("foo", foo1Code);
      foo1Macro.setDeferred(true);
      barMacro = makeMacroFunction("bar", barCode);
      barMacro.setDeferred(true);
    }
    interpreter.getContext().addGlobalMacro(foo1Macro);
    interpreter.getContext().addGlobalMacro(barMacro);

    EagerMacroFunction foo2Macro;
    String output;
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      foo2Macro = makeMacroFunction("foo", foo2Code);
      output = foo2Macro.reconstructImage();
    }
    assertThat(interpreter.render(output + "{{ foo('Foo') }}"))
      .isEqualTo("~^This is the Foo^~");
  }

  private EagerMacroFunction makeMacroFunction(String name, String code) {
    interpreter.render(code);
    return (EagerMacroFunction) interpreter.getContext().getGlobalMacro(name);
  }
}
