package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AstFilterChainTest {

  private Jinjava jinjava;
  private Map<String, Object> context;

  @Before
  public void setup() {
    jinjava =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
          .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
          .withEnableFilterChainOptimization(true)
          .build()
      );

    context = new HashMap<>();
    context.put("name", "  Hello World  ");
    context.put("text", "the quick brown fox jumps over the lazy dog");
    context.put("number", 12345);
    context.put("items", new String[] { "apple", "banana", "cherry" });
  }

  @Test
  public void itHandlesSingleFilter() {
    String result = jinjava.render("{{ name|trim }}", context);
    assertThat(result).isEqualTo("Hello World");
  }

  @Test
  public void itHandlesChainedFilters() {
    String result = jinjava.render("{{ name|trim|lower }}", context);
    assertThat(result).isEqualTo("hello world");
  }

  @Test
  public void itHandlesFiltersWithArguments() {
    String result = jinjava.render("{{ text|truncate(20)|upper }}", context);
    assertThat(result).isNotEmpty();
    assertThat(result).isUpperCase();
  }

  @Test
  public void itHandlesComplexFilterChain() {
    String result = jinjava.render(
      "{{ text|upper|replace('THE', 'a')|trim|lower|capitalize }}",
      context
    );
    assertThat(result).isNotEmpty();
  }

  @Test
  public void itHandlesFilterWithJoin() {
    String result = jinjava.render("{{ items|join(', ')|upper }}", context);
    assertThat(result).isEqualTo("APPLE, BANANA, CHERRY");
  }

  @Test
  public void itHandlesFilterWithStringConversion() {
    String result = jinjava.render("{{ number|string|length }}", context);
    assertThat(result).isEqualTo("5");
  }
}
