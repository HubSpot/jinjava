package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Before;

public abstract class BaseTagTest {
  public Jinjava jinjava;
  public JinjavaInterpreter interpreter;

  public Context context;

  @Before
  public void baseSetup() {
    jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava.newInterpreter());
    context = interpreter.getContext();
  }
}
