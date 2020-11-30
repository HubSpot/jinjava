package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.DoTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerDoTagTest extends DoTagTest {
  private Tag tag;
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig.newBuilder().withExecutionMode(new EagerExecutionMode()).build()
      );

    tag = new EagerDoTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/dotag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itHandlesDeferredDo() {
    context.put("foo", 2);
    expectedNodeInterpreter.assertExpectedOutput("handles-deferred-do");
  }

  /** This is broken in normal Jinjava as <code>hey</code> does not get output in quotes.
   * It works in Eager Jinjava as <code>hey</code> is quoted properly.
   */
  @Test
  @Override
  public void itResolvesExpressions() {
    String template = "{% set output = [] %}{% do output.append('hey') %}{{ output }}";
    assertThat(interpreter.render(template)).isEqualTo("['hey']");
  }
}
