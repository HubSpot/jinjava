package com.hubspot.jinjava.mode;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    assertThat(output).contains("name");
    assertThat(output).contains("upper");
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
    assertThat(output).contains("{% if unknown %}");
    assertThat(output).contains("Hello");
    assertThat(output).contains("{% endif %}");
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
    assertThat(output).contains("{% if unknown %}");
    assertThat(output).contains("Hello");
    assertThat(output).contains("{% else %}");
    assertThat(output).contains("Goodbye");
  }

  @Test
  public void itPreservesForTagWithUnknownIterable() {
    String output = interpreter.render("{% for item in items %}{{ item }}{% endfor %}");
    assertThat(output).contains("{% for item in items %}");
    assertThat(output).contains("{{ item }}");
    assertThat(output).contains("{% endfor %}");
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
    assertThat(output).contains("{% set x = unknown %}");
    assertThat(output).contains("{{ x }}");
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
    assertThat(output).contains("a-");
    assertThat(output).contains("b-");
    assertThat(output).contains("{{ unknown }}");
  }

  @Test
  public void itAllowsMultiPassRendering() {
    JinjavaInterpreter.popCurrent();
    try {
      Map<String, Object> firstPassContext = new HashMap<>();
      firstPassContext.put("staticValue", "STATIC");

      JinjavaConfig config = JinjavaConfig
        .newBuilder()
        .withExecutionMode(PreserveUndefinedExecutionMode.instance())
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
        )
        .build();

      String template = "{{ staticValue }} - {{ dynamicValue }}";
      String firstPassResult = jinjava
        .renderForResult(template, firstPassContext, config)
        .getOutput();

      assertThat(firstPassResult).contains("STATIC");
      assertThat(firstPassResult).contains("{{ dynamicValue }}");

      Map<String, Object> secondPassContext = new HashMap<>();
      secondPassContext.put("dynamicValue", "DYNAMIC");
      JinjavaConfig defaultConfig = JinjavaConfig
        .newBuilder()
        .withExecutionMode(DefaultExecutionMode.instance())
        .build();
      String secondPassResult = jinjava
        .renderForResult(firstPassResult, secondPassContext, defaultConfig)
        .getOutput();

      assertThat(secondPassResult).isEqualTo("STATIC - DYNAMIC");
    } finally {
      JinjavaInterpreter.pushCurrent(interpreter);
    }
  }

  @Test
  public void itPreservesComplexExpression() {
    interpreter.getContext().put("known", 5);
    String output = interpreter.render("{{ known + unknown }}");
    assertThat(output).contains("unknown");
    assertThat(output).contains("5");
  }

  @Test
  public void itPreservesExpressionTest() {
    String output = interpreter.render("{% if value is defined %}yes{% endif %}");
    assertThat(output).contains("{% if");
    assertThat(output).contains("defined");
  }
}
