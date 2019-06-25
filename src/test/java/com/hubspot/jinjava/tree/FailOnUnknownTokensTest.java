package com.hubspot.jinjava.tree;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FailOnUnknownTokensTest {
  private static Jinjava jinjava;

  @Before
  public void setUp() {
    JinjavaConfig.Builder builder = JinjavaConfig.newBuilder();
    builder.withFailOnUnknownTokens(true);
    JinjavaConfig config = builder.build();
    jinjava = new Jinjava(config);
  }

  @Test(expected = FatalTemplateErrorsException.class)
  public void itThrowsExceptionOnUnknownToken() {
    Map<String, String> context = new HashMap<>();
    context.put("token1", "test");
    String template = "hello {{ token1 }} and {{ token2 }}";
    jinjava.render(template, context);
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
  public void itReplacesTokensInContextButThrowsExceptionForOthers() {
    Map<String, String> context = new HashMap<>();
    context.put("animal", "lamb");
    context.put("fruit", "apple");
    String template = "{{ name }} has a {{ animal }}";
    try{
      jinjava.render(template, context);
    } catch (Exception ex) {
      assertThat(ex.getMessage()).containsIgnoringCase("unknown token found");
      assertThatExceptionOfType(UnknownTokenException.class);
    }

    template = "{{ name | default('mary') }} has a {{ animal }} and eats {{ fruit | default('mango')}}";
    assertThat(jinjava.render(template, context)).isEqualTo("mary has a lamb and eats apple");
  }
}
