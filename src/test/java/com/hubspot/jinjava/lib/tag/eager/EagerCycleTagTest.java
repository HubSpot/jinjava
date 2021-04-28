package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.CycleTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import org.junit.After;
import org.junit.Before;

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
    context.registerTag(new EagerForTag());
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }
}
