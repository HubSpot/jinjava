package com.hubspot.jinjava.lib.exptest;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class ContainsAllExpTest implements ExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (null == var || args.length == 0) {
      return false;
    }

    ForLoop loop = ObjectIterator.getLoop(args[0]);
    while (loop.hasNext()) {
      Object matchValue = loop.next();
      ForLoop varLoop = ObjectIterator.getLoop(var);
      boolean matches = false;
      while (varLoop.hasNext()) {
        if (Objects.equals(matchValue, args[0])) {
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

  private Optional<Iterator<?>> getIterator(Object var) {

    if (Iterator.class.isAssignableFrom(var.getClass())) {
      return Optional.of((Iterator<?>) var);
    }

    if (Iterable.class.isAssignableFrom(var.getClass())) {
      return Optional.of(((Iterable<?>) var).iterator());
    }

    return Optional.empty();
  }

  @Override
  public String getName() {
    return "containsall";
  }
}
