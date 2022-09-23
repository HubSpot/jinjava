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
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.DeferredValueUtils;
import java.util.HashMap;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerForTagTest extends ForTagTest {
  private static final long MAX_OUTPUT_SIZE = 5000L;
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    setupWithExecutionMode(EagerExecutionMode.instance());
    tag = new EagerForTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/fortag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  private void setupWithExecutionMode(ExecutionMode executionMode) {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(executionMode)
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itRegistersDeferredToken() {
    expectedNodeInterpreter.assertExpectedOutput("registers-eager-token");
    Optional<DeferredToken> maybeDeferredToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeDeferredToken).isPresent();
    assertThat(maybeDeferredToken.get().getSetDeferredWords())
      .containsExactlyInAnyOrder("item");
    assertThat(maybeDeferredToken.get().getUsedDeferredWords()).contains("deferred");
  }

  @Test
  public void itHandlesMultipleLoopVars() {
    expectedNodeInterpreter.assertExpectedOutput("handles-multiple-loop-vars");
    Optional<DeferredToken> maybeDeferredToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeDeferredToken).isPresent();
    assertThat(maybeDeferredToken.get().getSetDeferredWords())
      .containsExactlyInAnyOrder("item", "item2");
    assertThat(maybeDeferredToken.get().getUsedDeferredWords()).contains("deferred");
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
  public void itDefersVariablesThatLaterGetDeferred() {
    String result = interpreter.render(
      "{%- for i in range(0, deferred) %}\n" +
      "{{ foo ~ (1 + 2) }}\n" +
      "{% set foo = i %}\n" +
      "{% endfor %}"
    );
    assertThat(result)
      .isEqualTo(
        "{% for i in range(0, deferred) %}\n" +
        "{{ foo ~ 3 }}\n" +
        "{% set foo = i %}\n" +
        "{% endfor %}"
      );
    assertThat(interpreter.getContext().getDeferredNodes()).hasSize(0);
  }

  @Test
  public void itDoesntAllowChangesInDeferredForWithSameHashCode() {
    // Map with {'a':'a'} has the same hashcode as {'b':'b'} so we must differentiate
    String result = interpreter.render(
      "{% set foo = {'a': 'a'} -%}\n" +
      "{%- for i in range(0, deferred) %}\n" +
      "{{ 'bar' }}{{ foo }}\n" +
      "{% do foo.clear() %}\n" +
      "{% do foo.update({'b': 'b'}) %}\n" +
      "{% endfor %}\n" +
      "{{ foo }}"
    );
    assertThat(result)
      .isEqualTo(
        "{% set foo = {'a': 'a'} %}{% for i in range(0, deferred) %}\n" +
        "bar{{ foo }}\n" +
        "{% do foo.clear() %}\n" +
        "{% do foo.update({'b': 'b'}) %}\n" +
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

  @Test
  public void itDoesNotSwallowDeferredValueException() {
    interpreter.getContext().registerTag(new EagerDoTag());
    interpreter.getContext().registerTag(new EagerIfTag());
    interpreter.getContext().registerTag(new EagerSetTag());

    String input =
      "{% set my_list = [] %}" +
      "{% for i in range(30) %}" +
      "{{ my_list.append(i) }}" +
      "{% endfor %}" +
      "{% for i in [0, 1] %}" +
      "{% for j in deferred %}" +
      "{% if loop.first %}" +
      "{% do my_list.append(1) %}" +
      "{% endif %}" +
      "{% endfor %}" +
      "{% endfor %}" +
      "{{ my_list }}";
    EagerExecutionMode executionMode = new EagerExecutionMode() {

      @Override
      public boolean useEagerContextReverting() {
        return false;
      }
    };
    setupWithExecutionMode(executionMode);

    String render = interpreter.render(input);
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();

    HashMap<String, Object> deferredContextWithOriginalValues = DeferredValueUtils.getDeferredContextWithOriginalValues(
      interpreter.getContext()
    );
    setupWithExecutionMode(executionMode);
    interpreter.getContext().putAll(deferredContextWithOriginalValues);
    interpreter.getContext().put("deferred", "[]");

    interpreter.render(render);
    assertThat(interpreter.getContext().getDeferredTokens()).isEmpty();
  }

  @Test
  public void itRendersNestedForLoops() {
    String input =
      "{% for __ignored1__ in [0] %}\n" +
      "  {% set counter = [] %}\n" +
      "  {% for __ignored2__ in [0] %}\n" +
      "    {% for __ignored3__ in [0] %}\n" +
      "      {% do counter.append(1) %}\n" +
      "    {% endfor %}\n" +
      "  {% endfor %}\n" +
      "  {{ counter }}" +
      "{% endfor %}";

    EagerExecutionMode executionMode = new EagerExecutionMode() {

      @Override
      public boolean useEagerContextReverting() {
        return false;
      }
    };
    setupWithExecutionMode(executionMode);

    String output = interpreter.render(input);

    HashMap<String, Object> deferredContextWithOriginalValues = DeferredValueUtils.getDeferredContextWithOriginalValues(
      interpreter.getContext()
    );
    setupWithExecutionMode(DefaultExecutionMode.instance());
    interpreter.getContext().putAll(deferredContextWithOriginalValues);
    String output2 = interpreter.render(output);
    assertThat(output2.trim()).isEqualTo("[1]");
  }
}
