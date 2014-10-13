package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IsNoneExpTest implements ExpTest {

  @Override
  public String getName() {
    return "none";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var == null;
  }

}
