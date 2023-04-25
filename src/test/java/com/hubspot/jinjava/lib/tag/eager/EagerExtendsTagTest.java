package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.hubspot.jinjava.ExpectedTemplateInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ExtendsTagTest;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerExtendsTagTest extends ExtendsTagTest {
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
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );
    context.put("deferred", DeferredValue.instance());
    expectedTemplateInterpreter =
      new ExpectedTemplateInterpreter(jinjava, interpreter, "tags/eager/extendstag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itDefersBlockInExtendsChild() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-block-in-extends-child");
  }

  @Test
  public void itDefersBlockInExtendsChildSecondPass() {
    context.put("deferred", "Resolved now");
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-block-in-extends-child.expected"
    );
  }

  @Test
  public void itDefersSuperBlockWithDeferred() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-super-block-with-deferred");
  }

  @Test
  public void itDefersSuperBlockWithDeferredSecondPass() {
    context.put("deferred", "Resolved now");
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-super-block-with-deferred.expected"
    );
  }

  @Test
  public void itReconstructsDeferredOutsideBlock() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "reconstructs-deferred-outside-block"
    );
  }

  @Test
  public void itReconstructsDeferredOutsideBlockSecondPass() {
    context.put("deferred", "Resolved now");

    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-deferred-outside-block.expected"
    );
  }

  @Test
  public void itThrowsWhenDeferredExtendsTag() {
    interpreter.render(
      expectedTemplateInterpreter.getFixtureTemplate("throws-when-deferred-extends-tag")
    );
    assertThat(interpreter.getContext().getDeferredNodes()).hasSize(2);
  }

  @Override
  @Ignore
  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksInExtendingTemplate()
    throws IOException {
    super.itSetsErrorLineNumbersCorrectlyInBlocksInExtendingTemplate();
  }

  @Override
  @Ignore
  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplate()
    throws IOException {
    super.itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplate();
  }

  @Override
  @Ignore
  @Test
  public void itSetsErrorLineNumbersCorrectlyOutsideBlocksFromExtendedTemplate()
    throws IOException {
    super.itSetsErrorLineNumbersCorrectlyOutsideBlocksFromExtendedTemplate();
  }

  @Override
  @Ignore
  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplateInIncludedTemplate()
    throws IOException {
    super.itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplateInIncludedTemplate();
  }
}
