package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.mode.KeepUndefinedExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JinjavaTest {

  private JinjavaInterpreter interpreter;
  private Jinjava jinjava;
  Context globalContext = new Context();
  Context localContext; // ref to context created with global as parent

  @Before
  public void setup() {
    setupWithExecutionMode(EagerExecutionMode.instance());
  }

  protected void setupWithExecutionMode(ExecutionMode executionMode) {
    JinjavaInterpreter.popCurrent();
    jinjava = new Jinjava();
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          return Resources.toString(
            Resources.getResource(fullName),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withExecutionMode(executionMode)
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .withMaxMacroRecursionDepth(20)
      .withEnableRecursiveMacroCalls(true)
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
    try {
      assertThat(interpreter.getErrors()).isEmpty();
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itReconstructsMapWithNullValues() {
    interpreter.render("{% set foo = {'foo': null} %}");
    assertThat(interpreter.getContext().get("foo")).isInstanceOf(Map.class);
    assertThat((Map) interpreter.getContext().get("foo")).hasSize(1);
  }

  @Test
  public void itDefersNodeWhenModifiedInForLoop() {
    setupWithExecutionMode(KeepUndefinedExecutionMode.instance());
    assertThat(
      interpreter.render(
        """
        {%- set bar = 'bar' %}
        {%- set foo = 0 %}
        {%- for i in undefined %}
          {{ bar ~ foo ~ bar }}
           {% set foo = foo + 1 %}
        {% endfor %}
        """
      )
    )
      .isEqualTo(
        """
        {% set foo = 0 %}{% for i in undefined %}
          {{ 'bar' ~ foo ~ 'bar' }}
           {% set foo = foo + 1 %}
        {% endfor %}
        """
      );
  }

  @Test
  public void itDefersNodeForDotAccess() {
    setupWithExecutionMode(KeepUndefinedExecutionMode.instance());
    assertThat(interpreter.render("{{content.title}}")).isEqualTo("{{ content.title }}");
  }
}
