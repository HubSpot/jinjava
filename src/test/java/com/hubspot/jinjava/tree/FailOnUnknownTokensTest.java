package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FailOnUnknownTokensTest {

  private static Jinjava jinjava;

  @Before
  public void setUp() {
    JinjavaConfig.Builder builder = BaseJinjavaTest.newConfigBuilder();
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
    assertThat(renderedTemplate).isEqualTo("hello test and test1");
  }

  @Test
  public void itReplacesTokensWithDefaultValues() {
    Map<String, String> context = new HashMap<>();
    context.put("animal", "lamb");
    context.put("fruit", "apple");

    String template =
      "{{ name | default('mary') }} has a {{ animal }} and eats {{ fruit | default('mango')}}";
    assertThat(jinjava.render(template, context))
      .isEqualTo("mary has a lamb and eats apple");
  }

  @Test
  public void itAllowsNullReturningDoExpressions() {
    String template = "{% set object = {} %}{% do object.update({}) %}done";

    assertThat(jinjava.render(template, new HashMap<>())).isEqualTo("done");
  }

  @Test
  public void itAllowsNullReturningDoExpressionsInEagerMode() {
    Jinjava eagerJinjava = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withFailOnUnknownTokens(true)
        .withExecutionMode(EagerExecutionMode.instance())
        .build()
    );
    String template = "{% set object = {} %}{% do object.update({}) %}done";

    assertThat(eagerJinjava.render(template, new HashMap<>())).isEqualTo("done");
  }

  @Test
  public void itRejectsUnknownMethodsInDoExpressions() {
    String template = "{% set object = {} %}{% do object.missing() %}";

    assertThatThrownBy(() -> jinjava.render(template, new HashMap<>()))
      .hasMessageContaining("Cannot find method missing");
  }

  @Test
  public void itRejectsUnknownPropertiesAfterNullReturningMethods() {
    String template = "{% set object = {} %}{% do object.update({}).missing %}";

    assertThatThrownBy(() -> jinjava.render(template, new HashMap<>()))
      .hasMessageContaining("Unknown token found: object.update({}).missing");
  }

  @Test
  public void itReplacesTokensInContextButThrowsExceptionForOthers() {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
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
