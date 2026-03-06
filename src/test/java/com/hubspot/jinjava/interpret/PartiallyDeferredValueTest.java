package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.testobjects.PartiallyDeferredValueTestObjects;
import org.junit.Before;
import org.junit.Test;

public class PartiallyDeferredValueTest extends BaseInterpretingTest {

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
  public void itDefersNodeWhenCannotSerializePartiallyDeferredValue() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.BadSerialization());
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itDefersNodeWhenCannotCallPartiallyDeferredMapEntrySet() {
    interpreter
      .getContext()
      .put(
        "foo",
        new PartiallyDeferredValueTestObjects.BadEntrySet(
          ImmutableMap.of("resolved", "resolved")
        )
      );
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itDefersNodeWhenPyishSerializationFails() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.BadPyishSerializable());
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itSerializesWhenPyishSerializationIsGood() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.GoodPyishSerializable());
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ good.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  @Test
  public void itSerializesWhenEntrySetIsBadButItIsPyishSerializable() {
    interpreter
      .getContext()
      .put(
        "foo",
        new PartiallyDeferredValueTestObjects.BadEntrySetButPyishSerializable(
          ImmutableMap.of("resolved", "resolved")
        )
      );
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ hello.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  @Test
  public void itSerializesPartiallyDeferredValueIsInsideAMap() {
    interpreter
      .getContext()
      .put(
        "foo_map",
        new PyMap(
          ImmutableMap.of(
            "foo",
            new PartiallyDeferredValueTestObjects.GoodPyishSerializable()
          )
        )
      );
    assertThat(interpreter.render("{% set bar = foo_map %}{{ bar.foo.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo_map.foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo_map %}{{ bar.foo.deferred }}"))
      .isEqualTo("{{ good.deferred }}");
    assertThat(interpreter.render("{% set bar = foo_map.foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ good.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  @Test
  public void itSerializesPartiallyDeferredValueIsPutInsideAMap() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.GoodPyishSerializable());
    assertThat(
      interpreter.render("{% set bar = {'my_key': foo} %}{% print bar.my_key.resolved %}")
    )
      .isEqualTo("resolved");
    assertThat(
      interpreter.render("{% set bar = {'my_key': foo} %}{% print bar.my_key.deferred %}")
    )
      .isEqualTo("{% print good.deferred %}");
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  @Test
  public void itSerializesPartiallyDeferredValueIsPutInsideAMapInComplexExpression() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.GoodPyishSerializable());
    assertThat(
      interpreter.render(
        "{% set bar = {'my_key': foo} %}{% print (1 + 1 == 3 || bar.my_key.resolved) ~ '.' %}"
      )
    )
      .isEqualTo("resolved.");
    assertThat(
      interpreter.render(
        "{% set bar = {'my_key': foo} %}{% print (1 + 1 == 3 || bar.my_key.deferred) ~ '.' %}"
      )
    )
      .isEqualTo("{% print (false || good.deferred) ~ '.' %}");
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }

  @Test
  public void itSerializesPartiallyDeferredValueInsteadOfPreservingOriginalIdentifier() {
    interpreter
      .getContext()
      .put("foo", new PartiallyDeferredValueTestObjects.GoodPyishSerializable());
    assertThat(
      interpreter.render(
        "{% set list = [] %}{% set bar = foo %}{% do list.append(bar['resolved']) %}{% print list %}"
      )
    )
      .isEqualTo("['resolved']");
    assertThat(
      interpreter.render(
        "{% set list = [] %}{% set bar = foo %}{% do list.append(bar['deferred']) %}{% print list %}"
      )
    )
      .isEqualTo(
        "{% set list = [] %}{% do list.append(good['deferred']) %}{% print list %}"
      );
    assertThat(interpreter.getContext().getDeferredNodes()).isEmpty();
  }
}
