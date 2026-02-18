package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class EagerAstBinaryTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
      .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
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
    List<Object> fooList = new ArrayList<>();
    fooList.add("val");
    interpreter.getContext().put("foo_list", new PyList(fooList));
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
      interpreter.resolveELExpression("foo && deferred && foo_list.add(foo)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("'bar' && deferred && foo_list.add('bar')");
    }
  }

  @Test
  public void itShortCircuitsDeferredOr() {
    assertThat(interpreter.resolveELExpression("foo_list.add(foo) || deferred", -1))
      .isEqualTo(true);
    try {
      interpreter.resolveELExpression("deferred || foo_list.add(foo)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("deferred || foo_list.add('bar')");
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

  @Test
  public void itDoesNotShortCircuitNonModification() {
    assertThat(interpreter.resolveELExpression("foo_list[0] || deferred", -1))
      .isEqualTo("val");
    try {
      interpreter.resolveELExpression("deferred || foo_list[0]", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("deferred || 'val'");
    }
  }
}
