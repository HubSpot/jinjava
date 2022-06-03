package com.hubspot.jinjava;

import com.hubspot.jinjava.mode.NonRevertingEagerExecutionMode;
import org.junit.Before;
import org.junit.Ignore;

public class NonRevertingEagerTest extends EagerTest {

  @Override
  @Before
  public void setup() {
    setupWithExecutionMode(NonRevertingEagerExecutionMode.instance());
  }

  @Ignore
  @Override
  public void itCorrectlyDefersWithMultipleLoops() {
    super.itCorrectlyDefersWithMultipleLoops();
  }
}
