package com.hubspot.jinjava.mode;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PreserveUndefinedExecutionModeTest {

  private Jinjava jinjava;
  private JinjavaConfig config;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    config =
      JinjavaConfig
        .newBuilder()
        .withExecutionMode(PreserveUndefinedExecutionMode.instance())
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
        )
        .build();
  }

  private String render(String template) {
    return jinjava.renderForResult(template, new HashMap<>(), config).getOutput();
  }

  private String render(String template, Map<String, Object> context) {
    return jinjava.renderForResult(template, context, config).getOutput();
  }

  @Test
  public void itPreservesUndefinedExpression() {
    String output = render("{{ unknown }}");
    assertThat(output).isEqualTo("{{ unknown }}");
  }

  @Test
  public void itEvaluatesDefinedExpression() {
    Map<String, Object> context = new HashMap<>();
    context.put("name", "World");
    String output = render("{{ name }}", context);
    assertThat(output).isEqualTo("World");
  }

  @Test
  public void itPreservesUndefinedExpressionWithFilter() {
    String output = render("{{ name | upper }}");
    assertThat(output).isEqualTo("{{ name | upper }}");
  }

  @Test
  public void itPreservesUndefinedPropertyAccess() {
    String output = render("{{ obj.property }}");
    assertThat(output).isEqualTo("{{ obj.property }}");
  }

  @Test
  public void itPreservesNullValueExpression() {
    Map<String, Object> context = new HashMap<>();
    context.put("nullVar", null);
    String output = render("{{ nullVar }}", context);
    assertThat(output).isEqualTo("{{ nullVar }}");
  }

  @Test
  public void itPreservesMixedDefinedAndUndefined() {
    Map<String, Object> context = new HashMap<>();
    context.put("name", "World");
    String output = render("Hello {{ name }}, {{ unknown }}!", context);
    assertThat(output).isEqualTo("Hello World, {{ unknown }}!");
  }

  @Test
  public void itPreservesComplexExpression() {
    Map<String, Object> context = new HashMap<>();
    context.put("known", 5);
    String output = render("{{ known + unknown }}", context);
    assertThat(output).isEqualTo("{{ known + unknown }}");
  }

  @Test
  public void itAllowsMultiPassRendering() {
    Map<String, Object> firstPassContext = new HashMap<>();
    firstPassContext.put("staticValue", "STATIC");

    String template = "{{ staticValue }} - {{ dynamicValue }}";
    String firstPassResult = jinjava
      .renderForResult(template, firstPassContext, config)
      .getOutput();

    assertThat(firstPassResult).isEqualTo("STATIC - {{ dynamicValue }}");

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
  }

  @Test
  public void itEvaluatesForTagWithKnownIterable() {
    Map<String, Object> context = new HashMap<>();
    context.put("items", Arrays.asList("a", "b", "c"));
    String output = render("{% for item in items %}{{ item }}{% endfor %}", context);
    assertThat(output).isEqualTo("abc");
  }

  @Test
  public void itEvaluatesIfTagWithKnownCondition() {
    String output = render("{% if true %}Hello{% endif %}");
    assertThat(output).isEqualTo("Hello");
  }

  @Test
  public void itEvaluatesIfTagWithFalseCondition() {
    String output = render("{% if false %}Hello{% else %}Goodbye{% endif %}");
    assertThat(output).isEqualTo("Goodbye");
  }

  @Test
  public void itHandlesNestedUndefinedInKnownStructure() {
    Map<String, Object> context = new HashMap<>();
    context.put("items", Arrays.asList("a", "b"));
    String output = render(
      "{% for item in items %}{{ item }}-{{ unknown }}{% endfor %}",
      context
    );
    assertThat(output).isEqualTo("a-{{ unknown }}b-{{ unknown }}");
  }

  @Test
  public void itPreservesSetTagWithKnownRHSValue() {
    Map<String, Object> context = new HashMap<>();
    context.put("name", "World");
    String output = render("{% set x = name %}{{ x }}", context);
    // Set tag is preserved with evaluated RHS for multi-pass rendering
    assertThat(output).isEqualTo("{% set x = 'World' %}World");
  }

  @Test
  public void itPreservesSetTagWithUnknownRHS() {
    String output = render("{% set x = unknown %}{{ x }}");
    assertThat(output).isEqualTo("{% set x = unknown %}{{ x }}");
  }

  @Test
  public void itPreservesIfTagWithUnknownCondition() {
    String output = render("{% if unknown %}Hello{% endif %}");
    assertThat(output).isEqualTo("{% if unknown %}Hello{% endif %}");
  }

  @Test
  public void itPreservesIfElseWithUnknownCondition() {
    String output = render("{% if unknown %}Hello{% else %}Goodbye{% endif %}");
    assertThat(output).isEqualTo("{% if unknown %}Hello{% else %}Goodbye{% endif %}");
  }

  @Test
  public void itPreservesForTagWithUnknownIterable() {
    String output = render("{% for item in items %}{{ item }}{% endfor %}");
    assertThat(output).isEqualTo("{% for item in items %}{{ item }}{% endfor %}");
  }

  @Test
  public void itPreservesUndefinedInImportedMacro() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) -> {
      if (fullName.equals("macros.jinja")) {
        return "{% macro greet(name) %}Hello {{ name }}, {{ title }}!{% endmacro %}";
      }
      return "";
    });

    String template = "{% import 'macros.jinja' as m %}{{ m.greet('World') }}";
    String output = render(template);
    assertThat(output).isEqualTo("Hello World, {{ title }}!");
  }

  @Test
  public void itEvaluatesMacroWithAllDefinedVariables() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) -> {
      if (fullName.equals("macros.jinja")) {
        return "{% macro greet(name) %}Hello {{ name }}, {{ title }}!{% endmacro %}";
      }
      return "";
    });

    Map<String, Object> context = new HashMap<>();
    context.put("title", "Mr");
    String template = "{% import 'macros.jinja' as m %}{{ m.greet('World') }}";
    String output = render(template, context);
    // When all variables are defined, macro fully evaluates
    assertThat(output).isEqualTo("Hello World, Mr!");
  }

  @Test
  public void itPreservesUndefinedInFromImportMacro() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) -> {
      if (fullName.equals("macros.jinja")) {
        return "{% macro greet() %}Hello {{ unknown }}!{% endmacro %}";
      }
      return "";
    });

    String template = "{% from 'macros.jinja' import greet %}{{ greet() }}";
    String output = render(template);
    // Macro executes, but undefined variables are preserved
    assertThat(output).isEqualTo("Hello {{ unknown }}!");
  }
}
