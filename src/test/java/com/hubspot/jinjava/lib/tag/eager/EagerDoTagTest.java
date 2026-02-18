package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.lib.tag.DoTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerDoTagTest extends DoTagTest {

  private static final long MAX_OUTPUT_SIZE = 500L;
  private Tag tag;
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
          .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );

    tag = new EagerDoTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/dotag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itHandlesDeferredDo() {
    context.put("foo", 2);
    String template = "{% do deferred.append(foo*2) %}";
    assertThat(interpreter.render(template)).isEqualTo("{% do deferred.append(4) %}");
  }

  @Test
  public void itLimitsLength() {
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    context.setDeferredExecutionMode(true);
    interpreter.render(String.format("{%% do deferred.append('%s') %%}", tooLong));
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.OUTPUT_TOO_BIG);
  }
}
