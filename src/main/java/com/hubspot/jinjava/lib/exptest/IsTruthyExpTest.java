package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ObjectTruthValue;

public class IsTruthyExpTest implements ExpTest {

  @Override
  public String getName() {
    return "truthy";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return ObjectTruthValue.evaluate(var);
  }

}
