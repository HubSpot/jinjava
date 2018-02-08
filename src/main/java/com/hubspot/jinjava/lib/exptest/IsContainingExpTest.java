package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class IsContainingExpTest implements ExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (null == var || args.length == 0) {
      return false;
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      if (Objects.equals(loop.next(), args[0])) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getName() {
    return "containing";
  }
}
