package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FailOnUnknownTokensTest {
  private static Jinjava jinjava;

  @Before
  public void setUp() {
    JinjavaConfig.Builder builder = JinjavaConfig.newBuilder();
    builder.withFailOnUnknownTokens(true);
    JinjavaConfig config = builder.build();
    jinjava = new Jinjava(config);
  }

  @Test
  public void itReplaceTokensWithoutException() {
    Map<String, String> context = new HashMap<>();
    context.put("token1", "test");
    context.put("token2", "test1");
    String template = "hello {{ token1 }} and {{ token2 }}";
    String renderedTemplate = jinjava.render(template, context);
    assertEquals(renderedTemplate, "hello test and test1");
  }

  @Test
  public void itReplacesTokensWithDefaultValues() {
    Map<String, String> context = new HashMap<>();
    context.put("animal", "lamb");
    context.put("fruit", "apple");

    String template =
      "{{ name | default('mary') }} has a {{ animal }} and eats {{ fruit | default('mango')}}";
    assertEquals(jinjava.render(template, context), "mary has a lamb and eats apple");
  }

  @Test
  public void itReplacesTokensInContextButThrowsExceptionForOthers() {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();

    String template = "{{ name }} has a {{ animal }}";
    Node node = new TreeParser(jinjavaInterpreter, template).buildTree();
    assertThatThrownBy(() -> jinjavaInterpreter.render(node))
      .isInstanceOf(UnknownTokenException.class)
      .hasMessageContaining("Unknown token found: name");
  }
}
