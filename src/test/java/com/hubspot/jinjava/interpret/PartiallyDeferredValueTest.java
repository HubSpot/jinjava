package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

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
      return (T) appendable.append("");
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
}
