package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class IsWithinExpTest implements ExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (args == null || args.length == 0) {
      return false;
    }

    ForLoop loop = ObjectIterator.getLoop(args[0]);
    while (loop.hasNext()) {
      if (Objects.equals(loop.next(), var)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getName() {
    return "within";
  }
}
