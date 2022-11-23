package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.UnlessTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerUnlessTagTest extends UnlessTagTest {
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    tag = new EagerUnlessTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/unlesstag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itHandlesDeferredInRegular() {
    context.put("foo", true);
    expectedNodeInterpreter.assertExpectedOutput("handles-deferred-in-regular");
    Optional<DeferredToken> maybeEagerTagToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isEmpty();
  }

  @Test
  public void itHandlesDeferredInEager() {
    context.put("foo", 1);
    expectedNodeInterpreter.assertExpectedOutput("handles-deferred-in-eager");
    Optional<DeferredToken> maybeEagerTagToken = context
      .getDeferredTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getSetDeferredWords()).isEmpty();
    assertThat(maybeEagerTagToken.get().getUsedDeferredWords())
      .containsExactly("deferred");
  }
}
