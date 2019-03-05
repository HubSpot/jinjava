package com.hubspot.jinjava.lib.filter;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public abstract class AbstractSetFilter implements AdvancedFilter {

  protected Object parseArgs(JinjavaInterpreter interpreter, Object[] args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (a list to perform set function)");
    }

    return args[0];
  }

  protected Set<Object> objectToSet(Object var) {
    Set<Object> result = new LinkedHashSet<>();
    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      result.add(loop.next());
    }
    return result;
  }
}
