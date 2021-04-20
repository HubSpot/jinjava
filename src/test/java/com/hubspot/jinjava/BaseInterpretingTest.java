package com.hubspot.jinjava;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.After;
import org.junit.Before;

public abstract class BaseInterpretingTest extends BaseJinjavaTest {
  public JinjavaInterpreter interpreter;
  public Context context;

  @Before
  @Override
  public void baseSetup() {
    super.baseSetup();
    interpreter = new JinjavaInterpreter(jinjava.newInterpreter());
    context = interpreter.getContext();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void baseTeardown() {
    JinjavaInterpreter.popCurrent();
  }
}
