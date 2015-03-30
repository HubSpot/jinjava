package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("return true if object is a string")
public class IsStringExpTest implements ExpTest {

  @Override
  public String getName() {
    return "string";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var != null && var instanceof String;
  }

}
