package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import org.junit.Before;
import org.junit.Test;

public class EagerAstBinaryTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withExecutionMode(EagerExecutionMode.instance())
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .withMaxMacroRecursionDepth(5)
      .withEnableRecursiveMacroCalls(true)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      new Context(),
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);

    interpreter.getContext().put("deferred", DeferredValue.instance());
    interpreter.getContext().put("foo", "bar");
  }

  @Test
  public void itDoesNotShortCircuitIdentifier() {
    try {
      interpreter.resolveELExpression("foo && deferred && foo", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("'bar' && deferred && 'bar'");
    }
  }

  @Test
  public void itShortCircuitsDeferredAnd() {
    assertThat(interpreter.resolveELExpression("false && deferred", -1)).isEqualTo(false);

    try {
      interpreter.resolveELExpression("foo && deferred && range(1)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("'bar' && deferred && range(1)");
    }
  }

  @Test
  public void itShortCircuitsDeferredOr() {
    assertThat(interpreter.resolveELExpression("foo || deferred", -1)).isEqualTo("bar");
    try {
      interpreter.resolveELExpression("deferred || range(1)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("deferred || range(1)");
    }
  }

  @Test
  public void itDoesNotShortCircuitOtherOperators() {
    try {
      interpreter.resolveELExpression("deferred + range(1)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("deferred + [0]");
    }
  }
}
