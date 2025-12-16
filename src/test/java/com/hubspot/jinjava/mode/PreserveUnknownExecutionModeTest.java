package com.hubspot.jinjava.mode;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PreserveUnknownExecutionModeTest {

  private Jinjava jinjava;

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withExecutionMode(PreserveUnknownExecutionMode.instance())
      .build();
    jinjava = new Jinjava(config);
  }

  @Test
  public void itPreservesSimpleUnknownExpression() {
    String template = "{{ abc }}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{{ abc }}");
  }

  @Test
  public void itRendersKnownExpression() {
    String template = "{{ abc }}";
    Map<String, Object> context = new HashMap<>();
    context.put("abc", "hello");
    String result = jinjava.render(template, context);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  public void itPreservesMixedExpressions() {
    String template = "Hello {{ name }}! Value: {{ unknown }}";
    Map<String, Object> context = new HashMap<>();
    context.put("name", "World");
    String result = jinjava.render(template, context);
    assertThat(result).isEqualTo("Hello World! Value: {{ unknown }}");
  }

  @Test
  public void itPreservesIfBlockWithUnknownCondition() {
    String template = "{% if item %}content{% endif %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% if item %}content{% endif %}");
  }

  @Test
  public void itRendersIfBlockWithKnownCondition() {
    String template = "{% if item %}content{% endif %}";
    Map<String, Object> context = new HashMap<>();
    context.put("item", true);
    String result = jinjava.render(template, context);
    assertThat(result).isEqualTo("content");
  }

  @Test
  public void itRendersIfBlockWithFalseCondition() {
    String template = "{% if item %}content{% endif %}";
    Map<String, Object> context = new HashMap<>();
    context.put("item", false);
    String result = jinjava.render(template, context);
    assertThat(result).isEqualTo("");
  }

  @Test
  public void itPreservesIfElseBlockWithUnknownCondition() {
    String template = "{% if item %}yes{% else %}no{% endif %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% if item %}yes{% else %}no{% endif %}");
  }

  @Test
  public void itPreservesForLoopWithUnknownIterable() {
    String template = "{% for x in items %}{{ x }}{% endfor %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% for x in items %}{{ x }}{% endfor %}");
  }

  @Test
  public void itRendersForLoopWithKnownIterable() {
    String template = "{% for x in items %}{{ x }}{% endfor %}";
    Map<String, Object> context = new HashMap<>();
    context.put("items", java.util.Arrays.asList("a", "b", "c"));
    String result = jinjava.render(template, context);
    assertThat(result).isEqualTo("abc");
  }

  @Test
  public void itPreservesNestedStructuresWithUnknownVariables() {
    String template = "{% if a %}{% if b %}inner{% endif %}{% endif %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% if a %}{% if b %}inner{% endif %}{% endif %}");
  }

  @Test
  public void itPreservesExpressionWithFilters() {
    String template = "{{ name | upper }}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{{ name | upper }}");
  }

  @Test
  public void itPreservesComplexExpression() {
    String template = "{{ obj.property }}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{{ obj.property }}");
  }

  @Test
  public void itPreservesIncludeWithUnknownPath() {
    String template = "{% include unknown_path %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% include unknown_path %}");
  }

  @Test
  public void itRendersIncludeWithKnownPath() {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        @Override
        public String getString(
          String fullName,
          Charset encoding,
          com.hubspot.jinjava.interpret.JinjavaInterpreter interpreter
        ) throws IOException {
          if ("test.html".equals(fullName)) {
            return "included content";
          }
          throw new IOException("Template not found: " + fullName);
        }
      }
    );
    String template = "{% include 'test.html' %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("included content");
  }

  @Test
  public void itPreservesUnknownVariablesInIncludedTemplate() {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        @Override
        public String getString(
          String fullName,
          Charset encoding,
          com.hubspot.jinjava.interpret.JinjavaInterpreter interpreter
        ) throws IOException {
          if ("test.html".equals(fullName)) {
            return "Hello {{ unknown_var }}";
          }
          throw new IOException("Template not found: " + fullName);
        }
      }
    );
    String template = "{% include 'test.html' %}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("Hello {{ unknown_var }}");
  }

  @Test
  public void itPreservesSetWithUnknownValue() {
    String template = "{% set var = unknown %}{{ var }}";
    String result = jinjava.render(template, new HashMap<>());
    assertThat(result).isEqualTo("{% set var = unknown %}{{ var }}");
  }

  @Test
  public void itPreservesSetWithLiteralValue() {
    String template = "{% set a = 1 %}{{ a }}";
    String result = jinjava.render(template, new HashMap<>());
    // With deferredExecutionMode=true, set tag is preserved but {{ a }} is evaluated to 1
    assertThat(result).isEqualTo("{% set a = 1 %}1");
  }

  @Test
  public void itEvaluatesSetRightSideWhenPossible() {
    String template = "{% set a = 1 %}{% set b = a %}{{ b }}";
    String result = jinjava.render(template, new HashMap<>());
    // Both set tags are preserved with evaluated right-hand sides
    // {{ b }} is evaluated because b is known (set to 1)
    assertThat(result).isEqualTo("{% set a = 1 %}{% set b = 1 %}1");
  }
}
