package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.junit.Before;
import org.junit.Test;

public class ContextTest {

  private static final String RESOLVED_EXPRESSION = "exp";
  private static final String RESOLVED_FUNCTION = "func";
  private static final String RESOLVED_VALUE = "val";

  private Context context;

  @Before
  public void setUp() {
    context = new Context();
  }

  @Test
  public void itAddsResolvedValuesFromAnotherContextObject() {
    Context donor = new Context();
    donor.addResolvedValue(RESOLVED_VALUE);
    donor.addResolvedFunction(RESOLVED_FUNCTION);
    donor.addResolvedExpression(RESOLVED_EXPRESSION);

    assertThat(context.getResolvedValues()).doesNotContain(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).doesNotContain(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).doesNotContain(RESOLVED_EXPRESSION);

    context.addResolvedFrom(donor);

    assertThat(context.getResolvedValues()).contains(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).contains(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).contains(RESOLVED_EXPRESSION);
  }

  @Test
  public void itRecursivelyAddsValuesUpTheContextChain() {
    Context child = new Context(context);
    child.addResolvedValue(RESOLVED_VALUE);
    child.addResolvedFunction(RESOLVED_FUNCTION);
    child.addResolvedExpression(RESOLVED_EXPRESSION);

    assertThat(context.getResolvedValues()).contains(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).contains(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).contains(RESOLVED_EXPRESSION);
  }

  @Test
  public void itResetsGlobalContextAfterRender() {
    Jinjava jinjava = new Jinjava();
    Context globalContext = jinjava.getGlobalContext();

    RenderResult result = jinjava.renderForResult(
      "{{ foo + 1 }}",
      ImmutableMap.of("foo", 1)
    );

    assertThat(result.getOutput()).isEqualTo("2");
    assertThat(result.getContext().getResolvedExpressions()).containsOnly("foo + 1");
    assertThat(result.getContext().getResolvedValues()).containsOnly("foo");
    assertThat(globalContext.getResolvedExpressions()).isEmpty();
    assertThat(globalContext.getResolvedValues()).isEmpty();
  }

  @Test(expected = TemplateSyntaxException.class)
  public void itThrowsFromChildContext() throws Exception {
    Jinjava jinjava = new Jinjava();
    Context globalContext = jinjava.getGlobalContext();
    globalContext.registerFunction(
      new ELFunctionDefinition(
        "",
        "throw_exception",
        this.getClass().getDeclaredMethod("throwException")
      )
    );
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      interpreter.getContext().setThrowInterpreterErrors(true);
      interpreter.render(
        "{% macro throw() %}{{ throw_exception() }}{% endmacro %}{{ throw() }}"
      );
      fail("Did not throw an exception");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  public static void throwException() {
    throw new RuntimeException();
  }
}
