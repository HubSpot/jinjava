package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.lib.tag.ForTagTest;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class EagerForTagTest extends ForTagTest {
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void setup() {
    super.setup();
    context.registerTag(new EagerForTag());
    tag = new EagerForTag();
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/fortag");
  }

  @Test
  public void itRegistersEagerToken() {
    expectedNodeInterpreter.assertExpectedOutput("registers-eager-token");
    Optional<EagerTagToken> maybeEagerTagToken = context
      .getEagerTagTokens()
      .stream()
      .filter(e -> e.getTagToken().getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getDeferredHelpers()).containsExactly("item");
  }
}
