package com.hubspot.jinjava.lib.fn.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import org.junit.Test;

public class EagerMacroFunctionTest extends BaseInterpretingTest {

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

  private MacroFunction makeMacroFunction(String name, String code) {
    interpreter.render(code);
    return context.getGlobalMacro(name);
  }
}
