package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class UnionFilter implements AdvancedFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {

    LinkedHashSet<Object> result = new LinkedHashSet<>();

    Object attr = null;
    if (args.length > 0) {
      attr = args[0];
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      result.add(loop.next());
    }

    loop = ObjectIterator.getLoop(attr);
    while (loop.hasNext()) {
      result.add(loop.next());
    }

    return new ArrayList<>(result);
  }

  @Override
  public String getName() {
    return "union";
  }
}
