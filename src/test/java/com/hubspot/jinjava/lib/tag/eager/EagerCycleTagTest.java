package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.CycleTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerCycleTagTest extends CycleTagTest {

  private static final long MAX_OUTPUT_SIZE = 500L;
  private Tag tag;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );

    tag = new EagerCycleTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    context.registerTag(new EagerForTag());
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itAddCycleTagAsADeferredToken() {
    String template =
      "{% for item in deferred %}{% cycle 'item-1','item-2' %}{% endfor %}";
    assertThat(interpreter.render(template)).isEqualTo(template);
    Optional<DeferredToken> maybeDeferredToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeDeferredToken.isPresent());
    assertThat(maybeDeferredToken.get().getToken().getImage())
      .isEqualTo("{% cycle 'item-1','item-2' %}");
  }

  @Test
  public void itHandlesDeferredCycle() {
    interpreter.getContext().put("deferred", DeferredValue.instance());
    String template =
      "{% set l = [] %}{% for item in deferred %}{% cycle l.append(deferred),5 %}{% endfor %}{{ l }}";
    assertThat(interpreter.render(template)).isEqualTo(template);
  }
}
