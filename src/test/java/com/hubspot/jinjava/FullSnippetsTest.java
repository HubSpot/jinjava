package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FullSnippetsTest {
  private JinjavaInterpreter interpreter;
  private Jinjava jinjava;
  private ExpectedTemplateInterpreter expectedTemplateInterpreter;
  Context globalContext = new Context();
  Context localContext; // ref to context created with global as parent

  @Before
  public void setup() {
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
        )
          throws IOException {
          return Resources.toString(
            Resources.getResource(String.format("tags/macrotag/%s", fullName)),
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
    expectedTemplateInterpreter =
      new ExpectedTemplateInterpreter(jinjava, interpreter, "snippets");
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
  public void itDoesNotOverrideCallTagFromHigherScope() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "does-not-override-call-tag-from-higher-scope"
    );
  }
}
