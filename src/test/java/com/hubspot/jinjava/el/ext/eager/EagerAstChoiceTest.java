package com.hubspot.jinjava.el.ext.eager;

import static org.junit.Assert.assertEquals;
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
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class EagerAstChoiceTest extends BaseInterpretingTest {

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
    interpreter.getContext().put("foo", "foo val");
    interpreter = new JinjavaInterpreter(parentInterpreter);
    interpreter.getContext().put("deferred", DeferredValue.instance());
    List<Object> fooList = new ArrayList<>();
    fooList.add("val");
    interpreter.getContext().put("foo_list", new PyList(fooList));
  }

  @Test
  public void itShortCircuitsChoiceIdentifier() {
    try {
      interpreter.getContext().put("foo", "foo val");
      interpreter.getContext().put("bar", "bar val");
      interpreter.resolveELExpression(
        "deferred ? foo_list.add(foo) : foo_list.add(bar)",
        -1
      );
      fail("Should throw deferredParsingException");
    } catch (DeferredParsingException e) {
      Assertions
        .assertThat(e.getDeferredEvalResult())
        .isEqualTo("deferred ? foo_list.add('foo val') : foo_list.add('bar val')");
    }
  }

  @Test
  public void itDoesNotShortCircuitsChoiceYes() {
    try {
      interpreter.getContext().put("bar", "bar val");
      interpreter.resolveELExpression(
        "foo_list[0] == 'val' ? deferred : foo_list.add(bar)",
        -1
      );
      fail("Should throw deferredParsingException");
    } catch (DeferredParsingException e) {
      Assertions.assertThat(e.getDeferredEvalResult()).isEqualTo("deferred");
    }
  }

  @Test
  public void itDoesNotShortCircuitsChoiceNo() {
    try {
      interpreter.getContext().put("bar", "bar val");
      interpreter.resolveELExpression(
        "foo_list[0] == 'bar' ? foo_list.add(bar) : deferred",
        -1
      );
      fail("Should throw deferredParsingException");
    } catch (DeferredParsingException e) {
      Assertions.assertThat(e.getDeferredEvalResult()).isEqualTo("deferred");
    }
  }

  @Test
  public void itResolvesChoiceYes() {
    interpreter.getContext().put("bar", "bar val");
    interpreter.resolveELExpression(
      "foo_list[0] == 'val' ? foo_list.add(bar) : deferred",
      -1
    );
    PyList result = (PyList) interpreter.getContext().get("foo_list");
    assertEquals(result.size(), 2);
    assertEquals(result.get(1), "bar val");
  }

  @Test
  public void itResolvesChoiceNo() {
    interpreter.getContext().put("bar", "bar val");
    interpreter.resolveELExpression(
      "foo_list[0] == 'bar' ? deferred : foo_list.add(bar)",
      -1
    );
    PyList result = (PyList) interpreter.getContext().get("foo_list");
    assertEquals(result.size(), 2);
    assertEquals(result.get(1), "bar val");
  }
}
