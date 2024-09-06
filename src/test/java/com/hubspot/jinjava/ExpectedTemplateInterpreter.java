package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExpectedTemplateInterpreter {

  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;
  private String path;
  private boolean sensibleCurrentPath = false;

  public ExpectedTemplateInterpreter(
    Jinjava jinjava,
    JinjavaInterpreter interpreter,
    String path
  ) {
    this.jinjava = jinjava;
    this.interpreter = interpreter;
    this.path = path;
  }

  public static ExpectedTemplateInterpreter withSensibleCurrentPath(
    Jinjava jinjava,
    JinjavaInterpreter interpreter,
    String path
  ) {
    return new ExpectedTemplateInterpreter(jinjava, interpreter, path, true);
  }

  private ExpectedTemplateInterpreter(
    Jinjava jinjava,
    JinjavaInterpreter interpreter,
    String path,
    boolean sensibleCurrentPath
  ) {
    this.jinjava = jinjava;
    this.interpreter = interpreter;
    this.path = path;
    this.sensibleCurrentPath = sensibleCurrentPath;
  }

  public String assertExpectedOutput(String name) {
    String template = getFixtureTemplate(name);
    String output = JinjavaInterpreter.getCurrent().render(template);
    assertThat(JinjavaInterpreter.getCurrent().getContext().getDeferredNodes())
      .as("Ensure no deferred nodes were created")
      .isEmpty();
    assertThat(prettify(output.trim())).isEqualTo(prettify(expected(name).trim()));
    assertThat(prettify(JinjavaInterpreter.getCurrent().render(output).trim()))
      .isEqualTo(prettify(expected(name).trim()));
    return output;
  }

  public String assertExpectedOutputNonIdempotent(String name) {
    String template = getFixtureTemplate(name);
    String output = JinjavaInterpreter.getCurrent().render(template);
    assertThat(JinjavaInterpreter.getCurrent().getContext().getDeferredNodes())
      .as("Ensure no deferred nodes were created")
      .isEmpty();
    assertThat(prettify(output.trim())).isEqualTo(prettify(expected(name).trim()));
    return output;
  }

  public String assertExpectedNonEagerOutput(String name) {
    String output;
    try {
      JinjavaInterpreter preserveInterpreter = new JinjavaInterpreter(
        jinjava,
        jinjava.getGlobalContextCopy(),
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(DefaultExecutionMode.instance())
          .withNestedInterpretationEnabled(true)
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .withMaxMacroRecursionDepth(20)
          .withEnableRecursiveMacroCalls(true)
          .build()
      );
      JinjavaInterpreter.pushCurrent(preserveInterpreter);

      try (InterpreterScopeClosable ignored = preserveInterpreter.enterScope()) {
        preserveInterpreter.getContext().putAll(interpreter.getContext());
        String template = getFixtureTemplate(name);
        output = JinjavaInterpreter.getCurrent().render(template);
        assertThat(JinjavaInterpreter.getCurrent().getContext().getDeferredNodes())
          .as("Ensure no deferred nodes were created")
          .isEmpty();
        assertThat(output.trim()).isEqualTo(expected(name).trim());
      }
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    if (name.contains(".expected")) {
      String originalName = name.replace(".expected", "");
      try {
        JinjavaInterpreter preserveInterpreter = new JinjavaInterpreter(
          jinjava,
          jinjava.getGlobalContextCopy(),
          JinjavaConfig
            .newBuilder()
            .withExecutionMode(DefaultExecutionMode.instance())
            .withNestedInterpretationEnabled(true)
            .withLegacyOverrides(
              LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
            )
            .withMaxMacroRecursionDepth(20)
            .withEnableRecursiveMacroCalls(true)
            .build()
        );
        JinjavaInterpreter.pushCurrent(preserveInterpreter);

        preserveInterpreter.getContext().putAll(interpreter.getContext());
        String template = getFixtureTemplate(originalName);
        try (InterpreterScopeClosable ignored = preserveInterpreter.enterScope()) {
          output = JinjavaInterpreter.getCurrent().render(template);
          assertThat(JinjavaInterpreter.getCurrent().getContext().getDeferredNodes())
            .as("Ensure no deferred nodes were created")
            .isEmpty();
          assertThat(prettify(output.trim())).isEqualTo(prettify(expected(name).trim()));
        }
      } finally {
        JinjavaInterpreter.popCurrent();
      }
    }
    return output;
  }

  private String prettify(String string) {
    return string.replaceAll("([}%]})([^\\s])", "$1\\\\\n$2");
  }

  public String getFixtureTemplate(String name) {
    try {
      if (sensibleCurrentPath) {
        JinjavaInterpreter
          .getCurrent()
          .getContext()
          .getCurrentPathStack()
          .push(String.format("%s/%s.jinja", path, name), 0, 0);
      }
      return simplify(
        Resources.toString(
          Resources.getResource(String.format("%s/%s.jinja", path, name)),
          StandardCharsets.UTF_8
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String expected(String name) {
    try {
      return simplify(
        Resources.toString(
          Resources.getResource(String.format("%s/%s.expected.jinja", path, name)),
          StandardCharsets.UTF_8
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String simplify(String prettified) {
    return prettified.replaceAll("\\\\\n\\s*", "");
  }

  public String getDeferredFixtureTemplate(String templateLocation) {
    try {
      return Resources.toString(
        Resources.getResource("deferred/" + templateLocation),
        Charsets.UTF_8
      );
    } catch (IOException e) {
      return null;
    }
  }
}
