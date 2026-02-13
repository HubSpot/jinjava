package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.AllowlistMethodValidator;
import com.hubspot.jinjava.el.ext.MethodValidatorConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
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
      .withMethodValidator(
        AllowlistMethodValidator.create(
          MethodValidatorConfig
            .builder()
            .addDefaultAllowlistGroups()
            .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
              EagerAstDotTest.class.getCanonicalName()
            )
            .build()
        )
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
  }

  @Test
  public void itDefersWhenDotThrowsDeferredValueException() {
    interpreter.getContext().put("foo", new Foo());
    assertThat(interpreter.render("{{ foo.deferred }}")).isEqualTo("{{ foo.deferred }}");
  }

  @Test
  public void itResolvedDeferredMapWithDot() {
    interpreter.getContext().put("foo", new Foo());
    assertThat(interpreter.render("{{ foo.resolved }}")).isEqualTo("resolved");
  }

  @Test
  public void itResolvedNestedDeferredMapWithDot() {
    interpreter.getContext().put("foo_map", ImmutableMap.of("bar", new Foo()));
    assertThat(interpreter.render("{{ foo_map.bar.resolved }}")).isEqualTo("resolved");
  }

  @Test
  public void itDefersNodeWhenNestedDeferredMapDotThrowsDeferredValueException() {
    interpreter.getContext().put("foo_map", ImmutableMap.of("bar", new Foo()));
    assertThat(interpreter.render("{{ foo_map.bar.deferred }}"))
      .isEqualTo("{{ foo_map.bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  public static class Foo implements PartiallyDeferredValue {

    public String getDeferred() {
      throw new DeferredValueException("foo.deferred is deferred");
    }

    public String getResolved() {
      return "resolved";
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }
  }
}
