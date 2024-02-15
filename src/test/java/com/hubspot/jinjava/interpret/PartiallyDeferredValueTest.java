package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.junit.Before;
import org.junit.Test;

public class PartiallyDeferredValueTest extends BaseInterpretingTest {

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
  }

  @Test
  public void itDefersNodeWhenCannotSerializePartiallyDeferredValue() {
    interpreter.getContext().put("foo", new BadSerialization());
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
      .put("foo", new BadEntrySet(ImmutableMap.of("resolved", "resolved")));
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itDefersNodeWhenPyishSerializationFails() {
    interpreter.getContext().put("foo", new BadPyishSerializable());
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.resolved }}"))
      .isEqualTo("resolved");
    assertThat(interpreter.render("{% set bar = foo %}{{ bar.deferred }}"))
      .isEqualTo("{{ bar.deferred }}");
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itSerializesWhenPyishSerializationIsGood() {
    interpreter.getContext().put("foo", new GoodPyishSerializable());
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
        new BadEntrySetButPyishSerializable(ImmutableMap.of("resolved", "resolved"))
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
      .put("foo_map", new PyMap(ImmutableMap.of("foo", new GoodPyishSerializable())));
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
    interpreter.getContext().put("foo", new GoodPyishSerializable());
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
    interpreter.getContext().put("foo", new GoodPyishSerializable());
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

  public static class BadSerialization implements PartiallyDeferredValue {

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

  public static class BadEntrySet extends PyMap implements PartiallyDeferredValue {

    public BadEntrySet(Map<String, Object> map) {
      super(map);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      throw new DeferredValueException("entries are deferred");
    }

    @CheckForNull
    @Override
    public Object get(@CheckForNull Object key) {
      if ("deferred".equals(key)) {
        throw new DeferredValueException("deferred key");
      }
      return super.get(key);
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }
  }

  public static class BadPyishSerializable
    implements ReconstructiblePartiallyDeferredValue {

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

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      throw new DeferredValueException("I'm bad");
    }
  }

  public static class GoodPyishSerializable
    implements PartiallyDeferredValue, PyishSerializable {

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

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append("good");
    }
  }

  public static class BadEntrySetButPyishSerializable
    extends PyMap
    implements PartiallyDeferredValue, PyishSerializable {

    public BadEntrySetButPyishSerializable(Map<String, Object> map) {
      super(map);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      throw new DeferredValueException("entries are deferred");
    }

    @CheckForNull
    @Override
    public Object get(@CheckForNull Object key) {
      if ("deferred".equals(key)) {
        throw new DeferredValueException("deferred key");
      }
      return super.get(key);
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append("hello");
    }
  }
}
