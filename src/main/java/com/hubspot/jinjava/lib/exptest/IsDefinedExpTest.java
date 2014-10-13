package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IsDefinedExpTest implements ExpTest {

  @Override
  public String getName() {
    return "defined";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null;
  }

}
