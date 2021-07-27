package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.lib.tag.ForTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerForTagTest extends ForTagTest {
  private static final long MAX_OUTPUT_SIZE = 5000L;
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );
    tag = new EagerForTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/fortag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itRegistersEagerToken() {
    expectedNodeInterpreter.assertExpectedOutput("registers-eager-token");
    Optional<EagerToken> maybeEagerToken = context
      .getEagerTokens()
      .stream()
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerToken).isPresent();
    assertThat(maybeEagerToken.get().getSetDeferredWords())
      .containsExactlyInAnyOrder("item");
    assertThat(maybeEagerToken.get().getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itHandlesMultipleLoopVars() {
    expectedNodeInterpreter.assertExpectedOutput("handles-multiple-loop-vars");
    Optional<EagerToken> maybeEagerToken = context
      .getEagerTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerToken).isPresent();
    assertThat(maybeEagerToken.get().getSetDeferredWords())
      .containsExactlyInAnyOrder("item", "item2");
    assertThat(maybeEagerToken.get().getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itHandlesNestedDeferredForLoop() {
    context.put("food_types", ImmutableList.of("sandwich", "salad", "smoothie"));
    expectedNodeInterpreter.assertExpectedOutput("handles-nested-deferred-for-loop");
  }

  @Test
  public void itLimitsLength() {
    interpreter.render(
      String.format(
        "{%% for item in (range(1000, %s)) + deferred %%}{%% endfor %%}",
        MAX_OUTPUT_SIZE
      )
    );
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.OUTPUT_TOO_BIG);
  }

  @Test
  public void itDoesntAllowChangesInDeferredFor() {
    String result = interpreter.render(
      "{% set foo = [0] -%}\n" +
      "{%- for i in range(0, deferred) %}\n" +
      "{{ 'bar' }}{{ foo }}\n" +
      "{% do foo.append(1) %}\n" +
      "{% endfor %}\n" +
      "{{ foo }}"
    );
    assertThat(result)
      .isEqualTo(
        "{% set foo = [0] %}{% for i in range(0, deferred) %}\n" +
        "bar{{ foo }}\n" +
        "{% do foo.append(1) %}\n" +
        "{% endfor %}\n" +
        "{{ foo }}"
      );
    assertThat(interpreter.getContext().getDeferredNodes()).hasSize(0);
  }

  @Test
  public void itAllowsChangesInDeferredForToken() {
    String output = interpreter.render(
      "{% set foo = [0] %}\n" +
      "{% for i in range(foo.append(1) ? 0 : 1, deferred) %}\n" +
      "{{ i }}\n" +
      "{% endfor %}\n" +
      "{{ foo }}"
    );
    assertThat(output.trim())
      .isEqualTo(
        "{% for i in range(0, deferred) %}\n" + "{{ i }}\n" + "{% endfor %}\n" + "[0, 1]"
      );
  }

  @Test
  public void itDefersLoopVariable() {
    String output = interpreter.render(
      "{% for i in range(0, deferred) %}\n" +
      "{{ loop.index }}\n" +
      "{% endfor %}\n" +
      "{% for i in range(0, 2) -%}\n" +
      "{{ loop.index }}\n" +
      "{% endfor %}"
    );
    assertThat(output.trim())
      .isEqualTo(
        "{% for i in range(0, deferred) %}\n" +
        "{{ loop.index }}\n" +
        "{% endfor %}\n" +
        "1\n" +
        "2"
      );
  }
}
