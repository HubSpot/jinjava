package com.hubspot.jinjava;

import com.hubspot.jinjava.mode.NonRevertingEagerExecutionMode;
import org.junit.Before;

public class NonRevertingEagerTest extends EagerTest {

  @Override
  @Before
  public void setup() {
    setupWithExecutionMode(NonRevertingEagerExecutionMode.instance());
  }
}
