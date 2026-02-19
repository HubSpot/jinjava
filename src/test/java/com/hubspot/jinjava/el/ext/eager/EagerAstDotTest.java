package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.testobjects.EagerAstDotTestObjects;
import org.junit.Before;
import org.junit.Test;

public class EagerAstDotTest extends BaseInterpretingTest {

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
      .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
      .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
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
  }

  @Test
  public void itDefersWhenDotThrowsDeferredValueException() {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter.getContext().put("foo", new EagerAstDotTestObjects.Foo());
      assertThat(interpreter.render("{{ foo.deferred }}"))
        .isEqualTo("{{ foo.deferred }}");
    }
  }

  @Test
  public void itResolvedDeferredMapWithDot() {
    interpreter.getContext().put("foo", new EagerAstDotTestObjects.Foo());
    assertThat(interpreter.render("{{ foo.resolved }}")).isEqualTo("resolved");
  }

  @Test
  public void itResolvedNestedDeferredMapWithDot() {
    interpreter
      .getContext()
      .put("foo_map", ImmutableMap.of("bar", new EagerAstDotTestObjects.Foo()));
    assertThat(interpreter.render("{{ foo_map.bar.resolved }}")).isEqualTo("resolved");
  }

  @Test
  public void itDefersNodeWhenNestedDeferredMapDotThrowsDeferredValueException() {
    interpreter
      .getContext()
      .put("foo_map", ImmutableMap.of("bar", new EagerAstDotTestObjects.Foo()));
    assertThat(interpreter.render("{{ foo_map.bar.deferred }}"))
      .isEqualTo("{{ foo_map.bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }
}
