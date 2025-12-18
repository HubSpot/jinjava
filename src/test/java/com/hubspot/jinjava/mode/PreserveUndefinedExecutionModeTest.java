package com.hubspot.jinjava.mode;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PreserveUndefinedExecutionModeTest {

  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;
  private Context globalContext;
  private Context localContext;

  @Before
  public void setup() {
    JinjavaInterpreter.popCurrent();
    jinjava = new Jinjava();
    globalContext = new Context();
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withExecutionMode(PreserveUndefinedExecutionMode.instance())
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      globalContext,
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);
    localContext = interpreter.getContext();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itPreservesUndefinedExpression() {
    String output = interpreter.render("{{ unknown }}");
    assertThat(output).isEqualTo("{{ unknown }}");
  }

  @Test
  public void itEvaluatesDefinedExpression() {
    interpreter.getContext().put("name", "World");
    String output = interpreter.render("{{ name }}");
    assertThat(output).isEqualTo("World");
  }

  @Test
  public void itPreservesUndefinedExpressionWithFilter() {
    String output = interpreter.render("{{ name | upper }}");
    // TODO
    assertThat(output).isEqualTo("{{ filter:upper.filter(name, ____int3rpr3t3r____) }}");
  }

  @Test
  public void itPreservesUndefinedPropertyAccess() {
    String output = interpreter.render("{{ obj.property }}");
    assertThat(output).isEqualTo("{{ obj.property }}");
  }

  @Test
  public void itPreservesNullValueExpression() {
    interpreter.getContext().put("nullVar", null);
    String output = interpreter.render("{{ nullVar }}");
    assertThat(output).isEqualTo("{{ nullVar }}");
  }

  @Test
  public void itPreservesMixedDefinedAndUndefined() {
    interpreter.getContext().put("name", "World");
    String output = interpreter.render("Hello {{ name }}, {{ unknown }}!");
    assertThat(output).isEqualTo("Hello World, {{ unknown }}!");
  }

  @Test
  public void itPreservesIfTagWithUnknownCondition() {
    String output = interpreter.render("{% if unknown %}Hello{% endif %}");
    assertThat(output).isEqualTo("{% if unknown %}Hello{% endif %}");
  }

  @Test
  public void itEvaluatesIfTagWithKnownCondition() {
    String output = interpreter.render("{% if true %}Hello{% endif %}");
    assertThat(output).isEqualTo("Hello");
  }

  @Test
  public void itEvaluatesIfTagWithFalseCondition() {
    String output = interpreter.render("{% if false %}Hello{% else %}Goodbye{% endif %}");
    assertThat(output).isEqualTo("Goodbye");
  }

  @Test
  public void itPreservesIfElseWithUnknownCondition() {
    String output = interpreter.render(
      "{% if unknown %}Hello{% else %}Goodbye{% endif %}"
    );
    assertThat(output).isEqualTo("{% if unknown %}Hello{% else %}Goodbye{% endif %}");
  }

  @Test
  public void itPreservesForTagWithUnknownIterable() {
    String output = interpreter.render("{% for item in items %}{{ item }}{% endfor %}");
    assertThat(output).isEqualTo("{% for item in items %}{{ item }}{% endfor %}");
  }

  @Test
  public void itEvaluatesForTagWithKnownIterable() {
    interpreter.getContext().put("items", Arrays.asList("a", "b", "c"));
    String output = interpreter.render("{% for item in items %}{{ item }}{% endfor %}");
    assertThat(output).isEqualTo("abc");
  }

  @Test
  public void itPreservesSetTagWithUnknownRHS() {
    String output = interpreter.render("{% set x = unknown %}{{ x }}");
    assertThat(output).isEqualTo("{% set x = unknown %}{{ x }}");
  }

  @Test
  public void itEvaluatesSetTagWithKnownRHSValue() {
    interpreter.getContext().put("name", "World");
    String output = interpreter.render("{% set x = name %}{{ x }}");
    assertThat(output).isEqualTo("World");
  }

  @Test
  public void itHandlesNestedUndefinedInKnownStructure() {
    interpreter.getContext().put("items", Arrays.asList("a", "b"));
    String output = interpreter.render(
      "{% for item in items %}{{ item }}-{{ unknown }}{% endfor %}"
    );
    // TODO
    assertThat(output)
      .isEqualTo(
        "{% for __ignored__ in [0] %}a-{{ unknown }}b-{{ unknown }}{% endfor %}"
      );
  }

  @Test
  public void itPreservesComplexExpression() {
    interpreter.getContext().put("known", 5);
    String output = interpreter.render("{{ known + unknown }}");
    assertThat(output).isEqualTo("{{ 5 + unknown }}");
  }

  @Test
  public void itPreservesExpressionTest() {
    String output = interpreter.render("{% if value is defined %}yes{% endif %}");
    // TODO
    assertThat(output)
      .isEqualTo(
        "{% if exptest:defined.evaluate(value, ____int3rpr3t3r____) %}yes{% endif %}"
      );
  }
}
