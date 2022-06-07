package com.hubspot.jinjava;

import com.hubspot.jinjava.mode.NonRevertingEagerExecutionMode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NonRevertingEagerTest extends EagerTest {

  @Override
  @Before
  public void setup() {
    setupWithExecutionMode(NonRevertingEagerExecutionMode.instance());
  }

  @Ignore
  @Override
  @Test
  public void itCorrectlyDefersWithMultipleLoops() {
    super.itCorrectlyDefersWithMultipleLoops();
  }
}
