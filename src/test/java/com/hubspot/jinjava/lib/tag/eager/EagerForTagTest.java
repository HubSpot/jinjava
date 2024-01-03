package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ForTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.List;
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
            LegacyOverrides
              .newBuilder()
              .withUsePyishObjectMapper(true)
              .withKeepNullableLoopValues(true)
              .build()
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
  public void itRegistersDeferredToken() {
    expectedNodeInterpreter.assertExpectedOutput("registers-eager-token");
    Optional<DeferredToken> maybeDeferredToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeDeferredToken).isPresent();
    assertThat(maybeDeferredToken.get().getSetDeferredWords()).isEmpty();
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
    assertThat(maybeDeferredToken.get().getSetDeferredWords()).isEmpty();
    assertThat(maybeDeferredToken.get().getUsedDeferredWords()).contains("deferred");
  }

  @Test
  public void itHandlesNestedDeferredForLoop() {
    context.put("food_types", ImmutableList.of("sandwich", "salad", "smoothie"));
    expectedNodeInterpreter.assertExpectedOutput("handles-nested-deferred-for-loop");
  }

  @Test
  public void itLimitsLength() {
    String out = interpreter.render(
      String.format(
        "{%% for item in (range(1000, %s)) + deferred %%}{%% endfor %%}",
        MAX_OUTPUT_SIZE
      )
    );
    assertThat(interpreter.getContext().getDeferredTokens()).hasSize(1);
  }

  @Test
  public void itUsesDeferredExecutionModeWhenChildrenAreLarge() {
    assertThat(
        interpreter.render(
          String.format(
            "{%% for item in range(%d) %%}1234567890{%% endfor %%}",
            MAX_OUTPUT_SIZE / 10 - 1
          )
        )
      )
      .hasSize((int) MAX_OUTPUT_SIZE - 10);
    assertThat(interpreter.getContext().getDeferredTokens()).isEmpty();
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
    assertThat(interpreter.getErrors()).isEmpty();
    String tooBigInput = String.format(
      "{%% for item in range(%d) %%}1234567890{%% endfor %%}",
      MAX_OUTPUT_SIZE / 10 + 1
    );
    assertThat(interpreter.render(tooBigInput)).isEqualTo(tooBigInput);
    assertThat(interpreter.getContext().getDeferredTokens()).hasSize(1);
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
    assertThat(interpreter.getErrors()).isEmpty();
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
        "{% set foo = {'a': 'a'}  %}{% for i in range(0, deferred) %}\n" +
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
  public void itCanNowHandleModificationInPartiallyDeferredLoop() {
    interpreter.getContext().registerTag(new EagerDoTag());
    interpreter.getContext().registerTag(new EagerIfTag());
    interpreter.getContext().registerTag(new EagerSetTag());

    String input =
      "{% set my_list = [] %}" +
      "{% for i in range(401) %}" +
      "{% do my_list.append(i) %}" +
      "{% endfor %}" +
      "{% for i in my_list.append(-1) ? [0, 1] : [0] %}" +
      "{% for j in deferred %}" +
      "{% if loop.first %}" +
      "{% do my_list.append(i) %}" +
      "{% endif %}" +
      "{% endfor %}" +
      "{% endfor %}" +
      "{{ my_list }}";
    String initialResult = interpreter.render(input);
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
    interpreter.getContext().put("deferred", ImmutableList.of(1, 2));
    interpreter.render(initialResult);
    assertThat(interpreter.getContext().get("my_list")).isInstanceOf(List.class);
    assertThat((List<Long>) interpreter.getContext().get("my_list"))
      .as(
        "Appends 401 numbers and then appends '-1', running the 'i' loop twice," +
        "which runs the 'j' loop, the first time appending the value of 'i', which will be '0', then '1'"
      )
      .hasSize(404)
      .containsSequence(400L, -1L, 0L, 1L);
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  public static boolean inForLoop() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    return interpreter.getContext().isInForLoop();
  }
}
