package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.IfTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerIfTagTest extends IfTagTest {
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig.newBuilder().withExecutionMode(new EagerExecutionMode()).build()
      );
    tag = new EagerIfTag();
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/iftag");
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
    Optional<EagerToken> maybeEagerTagToken = context
      .getEagerTokens()
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
    Optional<EagerToken> maybeEagerTagToken = context
      .getEagerTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getSetDeferredWords()).isEmpty();
    assertThat(maybeEagerTagToken.get().getUsedDeferredWords())
      .containsExactlyInAnyOrder("deferred");
  }

  @Test
  public void itHandlesOnlyDeferredElif() {
    context.put("foo", 1);
    expectedNodeInterpreter.assertExpectedOutput("handles-only-deferred-elif");
    Optional<EagerToken> maybeEagerTagToken = context
      .getEagerTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals("elif"))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getSetDeferredWords()).isEmpty();
    assertThat(maybeEagerTagToken.get().getUsedDeferredWords())
      .containsExactlyInAnyOrder("deferred");
  }

  @Test
  public void itRemovesImpossibleIfBlocks() {
    context.put("foo", 1);
    expectedNodeInterpreter.assertExpectedOutput("removes-impossible-if-blocks");
    Optional<EagerToken> maybeEagerTagToken = context
      .getEagerTokens()
      .stream()
      .filter(e -> e.getToken() instanceof TagToken)
      .filter(e -> ((TagToken) e.getToken()).getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getSetDeferredWords()).isEmpty();
    assertThat(maybeEagerTagToken.get().getUsedDeferredWords())
      .containsExactlyInAnyOrder("deferred");
  }
}
