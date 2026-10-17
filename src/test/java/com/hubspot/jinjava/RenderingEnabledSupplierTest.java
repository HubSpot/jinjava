package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.RenderingDisabledException;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;

public class RenderingEnabledSupplierTest {

  private AtomicBoolean renderingEnabled;
  private Jinjava jinjava;

  @Before
  public void setup() {
    renderingEnabled = new AtomicBoolean(true);
    jinjava =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withRenderingEnabledSupplier(renderingEnabled::get)
          .build()
      );
  }

  @Test
  public void itRendersByDefault() {
    assertThat(new Jinjava().render("{{ foo }}", ImmutableMap.of("foo", "bar")))
      .isEqualTo("bar");
  }

  @Test
  public void itRendersWhenEnabled() {
    assertThat(jinjava.render("{{ foo }}", ImmutableMap.of("foo", "bar")))
      .isEqualTo("bar");
  }

  @Test
  public void itReturnsDisabledErrorWhenDisabled() {
    renderingEnabled.set(false);

    RenderResult result = jinjava.renderForResult(
      "{{ foo }}",
      ImmutableMap.of("foo", "bar")
    );

    assertThat(result.getOutput()).isEmpty();
    assertThat(result.getErrors())
      .singleElement()
      .satisfies(error -> {
        assertThat(error.getSeverity()).isEqualTo(ErrorType.FATAL);
        assertThat(error.getReason()).isEqualTo(ErrorReason.DISABLED);
        assertThat(error.getException()).isInstanceOf(RenderingDisabledException.class);
      });
  }

  @Test
  public void itThrowsFromRenderWhenDisabled() {
    renderingEnabled.set(false);

    assertThatThrownBy(() -> jinjava.render("{{ foo }}", ImmutableMap.of("foo", "bar")))
      .isInstanceOf(FatalTemplateErrorsException.class);
  }

  @Test
  public void itChecksSupplierOnEveryRender() {
    assertThat(jinjava.render("{{ foo }}", ImmutableMap.of("foo", "bar")))
      .isEqualTo("bar");

    renderingEnabled.set(false);
    assertThat(jinjava.renderForResult("{{ foo }}", ImmutableMap.of()).getErrors())
      .isNotEmpty();

    renderingEnabled.set(true);
    assertThat(jinjava.render("{{ foo }}", ImmutableMap.of("foo", "baz")))
      .isEqualTo("baz");
  }

  @Test
  public void itPreventsInterpreterRenderingWhenDisabled() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    renderingEnabled.set(false);

    assertThatThrownBy(() -> interpreter.render("{{ foo }}"))
      .isInstanceOf(RenderingDisabledException.class);
    assertThatThrownBy(() -> interpreter.render(interpreter.parse("{{ foo }}")))
      .isInstanceOf(RenderingDisabledException.class);
    assertThatThrownBy(() -> interpreter.renderFlat("{{ foo }}"))
      .isInstanceOf(RenderingDisabledException.class);
  }
}
