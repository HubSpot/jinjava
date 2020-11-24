package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExpectedTemplateInterpreter {
  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;
  private String path;

  public ExpectedTemplateInterpreter(
    Jinjava jinjava,
    JinjavaInterpreter interpreter,
    String path
  ) {
    this.jinjava = jinjava;
    this.interpreter = interpreter;
    this.path = path;
  }

  public String assertExpectedOutput(String name) {
    String template = getFixtureTemplate(name);
    String output = JinjavaInterpreter.getCurrent().render(template);
    assertThat(output.trim()).isEqualTo(expected(name).trim());
    assertThat(JinjavaInterpreter.getCurrent().render(output).trim())
      .isEqualTo(expected(name).trim());
    return output;
  }

  public String assertExpectedNonEagerOutput(String name) {
    JinjavaInterpreter preserveInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContextCopy(),
      JinjavaConfig.newBuilder().withExecutionMode(new DefaultExecutionMode()).build()
    );
    try {
      JinjavaInterpreter.pushCurrent(preserveInterpreter);

      preserveInterpreter.getContext().putAll(interpreter.getContext());
      return assertExpectedOutput(name);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  private String getFixtureTemplate(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("%s/%s.jinja", path, name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String expected(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("%s/%s.expected.jinja", path, name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
