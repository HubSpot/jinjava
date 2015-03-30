package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("Return true if the object is an odd number")
public class IsOddExpTest implements ExpTest {

  @Override
  public String getName() {
    return "odd";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if(var == null || !Number.class.isAssignableFrom(var.getClass())) {
      return false;
    }
    
    return ((Number) var).intValue() % 2 != 0;
  }

}
