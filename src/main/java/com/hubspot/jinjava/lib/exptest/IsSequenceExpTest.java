package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("Return true if the variable is a sequence. Sequences are variables that are iterable.")
public class IsSequenceExpTest implements ExpTest {

  @Override
  public String getName() {
    return "sequence";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null && (var.getClass().isArray() || Iterable.class.isAssignableFrom(var.getClass()));
  }

}
