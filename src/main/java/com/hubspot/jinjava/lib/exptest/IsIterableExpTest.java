package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("Return true if the object is iterable (sequence, dict, etc)")
public class IsIterableExpTest implements ExpTest {

  @Override
  public String getName() {
    return "iterable";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null && (var.getClass().isArray() || Iterable.class.isAssignableFrom(var.getClass()));
  }

}
