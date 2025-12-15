package com.hubspot.jinjava.mode;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
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
}
