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
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.testobjects.EagerAstDotTestObjects;
import org.junit.Before;
import org.junit.Test;

public class EagerAstDotTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
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
    assertThat(interpreter.getErrors())
      .extracting(TemplateError::getSeverity)
      .containsExactly(ErrorType.FATAL);
  }

  @Test
  public void itResolvesRecordComponents() {
    interpreter
      .getContext()
      .put("record", new EagerAstDotTestObjects.RecordValue("resolved"));

    assertThat(interpreter.render("{{ record.value }} {{ record['value'] }}"))
      .isEqualTo("resolved resolved");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itResolvesComponentsAfterARecordIsAvailable() {
    String partiallyRendered = interpreter.render("{{ deferred.value }}");
    assertThat(partiallyRendered).isEqualTo("{{ deferred.value }}");

    interpreter
      .getContext()
      .put("deferred", new EagerAstDotTestObjects.RecordValue("resolved"));
    assertThat(interpreter.render(partiallyRendered)).isEqualTo("resolved");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersWhenRecordAccessorThrowsDeferredValueException() {
    interpreter
      .getContext()
      .put("record", new EagerAstDotTestObjects.DeferredRecordValue(null));
    String partiallyRendered = interpreter.render("{{ record.value }}");
    assertThat(partiallyRendered).isEqualTo("{{ record.value }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();

    interpreter
      .getContext()
      .put("record", new EagerAstDotTestObjects.DeferredRecordValue("resolved"));
    assertThat(interpreter.render(partiallyRendered)).isEqualTo("resolved");
    assertThat(interpreter.getErrors()).isEmpty();
  }
}
