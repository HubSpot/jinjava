package com.hubspot.jinjava.lib.filter;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public abstract class AbstractSetFilter implements AdvancedFilter {

  protected Object parseArgs(Object[] args) {
    return args.length > 0 ? args[0] : null;
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
