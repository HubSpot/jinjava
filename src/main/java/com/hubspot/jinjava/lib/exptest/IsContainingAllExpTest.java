package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class IsContainingAllExpTest implements ExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (null == var || args.length == 0 || args[0] == null) {
      return false;
    }

    ForLoop loop = ObjectIterator.getLoop(args[0]);
    while (loop.hasNext()) {
      Object matchValue = loop.next();
      ForLoop varLoop = ObjectIterator.getLoop(var);
      boolean matches = false;
      while (varLoop.hasNext()) {
        if (Objects.equals(matchValue, varLoop.next())) {
          matches = true;
          break;
        }
      }
      if (!matches) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String getName() {
    return "containingall";
  }
}
