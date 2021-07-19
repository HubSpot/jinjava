package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.ExpectedTemplateInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.IncludeTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.io.IOException;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerIncludeTagTest extends IncludeTagTest {
  private ExpectedTemplateInterpreter expectedTemplateInterpreter;

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
    context.put("deferred", DeferredValue.instance());
    expectedTemplateInterpreter =
      new ExpectedTemplateInterpreter(jinjava, interpreter, "tags/eager/includetag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itIncludesDeferred() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent("includes-deferred");
    assertThat(
        context
          .getEagerTokens()
          .stream()
          .flatMap(eagerToken -> eagerToken.getUsedDeferredWords().stream())
          .collect(Collectors.toSet())
      )
      .isEmpty();
    assertThat(
        context
          .getEagerTokens()
          .stream()
          .flatMap(eagerToken -> eagerToken.getSetDeferredWords().stream())
          .collect(Collectors.toSet())
      )
      .containsExactlyInAnyOrder("foo");
  }

  @Override
  @Test
  @Ignore
  public void itSetsErrorLineNumbersCorrectly() throws IOException {
    super.itSetsErrorLineNumbersCorrectly();
  }

  @Override
  @Test
  @Ignore
  public void itSetsErrorLineNumbersCorrectlyTwoLevelsDeep() throws IOException {
    super.itSetsErrorLineNumbersCorrectlyTwoLevelsDeep();
  }
}
