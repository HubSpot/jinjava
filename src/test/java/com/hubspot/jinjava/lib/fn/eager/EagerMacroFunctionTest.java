package com.hubspot.jinjava.lib.fn.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
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
    MacroFunction macroFunction = makeMacroFunction(name, code);
    EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
      name,
      macroFunction,
      interpreter
    );
    assertThat(eagerMacroFunction.reconstructImage()).isEqualTo(code);
  }

  @Test
  public void itResolvesFromContext() {
    context.put("foobar", "resolved");
    String name = "foo";
    String code = "{% macro foo(bar) %}{{ foobar }} and {{ bar }}{% endmacro %}";
    MacroFunction macroFunction = makeMacroFunction(name, code);
    EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
      name,
      macroFunction,
      interpreter
    );
    assertThat(eagerMacroFunction.reconstructImage())
      .isEqualTo("{% macro foo(bar) %}resolved and {{ bar }}{% endmacro %}");
  }

  @Test
  public void itReconstructsForAliasedName() {
    String name = "foo";
    String fullName = "local." + name;
    String codeFormat = "{%% macro %s(bar) %%}It's: {{ bar }}{%% endmacro %%}";
    MacroFunction macroFunction = makeMacroFunction(
      name,
      String.format(codeFormat, name)
    );
    EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
      fullName,
      macroFunction,
      interpreter
    );
    assertThat(eagerMacroFunction.reconstructImage())
      .isEqualTo(String.format(codeFormat, fullName));
  }

  @Test
  public void itReconstructsImageWithNamedParams() {
    String name = "foo";
    String code = "{% macro foo(bar, baz=0) %}It's: {{ bar }}, {{ baz }}{% endmacro %}";
    MacroFunction macroFunction = makeMacroFunction(name, code);
    EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
      name,
      macroFunction,
      interpreter
    );
    assertThat(eagerMacroFunction.reconstructImage()).isEqualTo(code);
  }

  @Test
  public void itPartiallyEvaluatesMacroFunction() {
    // Put this test here because it's only used in eager execution
    context.put("deferred", DeferredValue.instance());
    MacroFunction macroFunction = makeMacroFunction(
      "foo",
      "{% macro foo(bar) %}It's: {{ bar }}, {{ deferred }}{% endmacro %}"
    );
    assertThatThrownBy(() -> macroFunction.evaluate("Bar"))
      .isInstanceOf(DeferredValueException.class);
    try (TemporaryValueClosable<Boolean> ignored = context.withPartialMacroEvaluation()) {
      assertThat(macroFunction.evaluate("Bar")).isEqualTo("It's: Bar, {{ deferred }}");
    }
  }

  @Test
  public void itDoesNotAllowStackOverflow() {
    String name = "rec";
    String code =
      "{% macro rec(num=0) %}{% if num > 0 %}{{ num }}-{{ rec(num - 1)}}{% endif %}{% endmacro %}";
    MacroFunction macroFunction = makeMacroFunction(name, code);
    String output;
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
        name,
        macroFunction,
        interpreter
      );
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
    MacroFunction foo1Macro = makeMacroFunction("foo", foo1Code);
    foo1Macro.setDeferred(true);
    MacroFunction barMacro = makeMacroFunction("bar", barCode);
    barMacro.setDeferred(true);
    interpreter.getContext().addGlobalMacro(foo1Macro);
    interpreter.getContext().addGlobalMacro(barMacro);
    MacroFunction foo2Macro;
    String output;
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      foo2Macro = makeMacroFunction("foo", foo2Code);
      EagerMacroFunction eagerMacroFunction = new EagerMacroFunction(
        "foo",
        foo2Macro,
        interpreter
      );
      output = eagerMacroFunction.reconstructImage();
    }
    assertThat(interpreter.render(output + "{{ foo('Foo') }}"))
      .isEqualTo("~^This is the Foo^~");
  }

  private MacroFunction makeMacroFunction(String name, String code) {
    interpreter.render(code);
    return interpreter.getContext().getGlobalMacro(name);
  }
}
