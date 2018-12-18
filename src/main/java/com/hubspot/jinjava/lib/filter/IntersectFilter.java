package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Returns a list containing elements present in both lists",
    params = {
        @JinjavaParam(value = "value", type = "sequence", desc = "The first list"),
        @JinjavaParam(value = "list", type = "sequence", desc = "The second list")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3]|intersect([2, 3, 4]) }}")
    })
public class IntersectFilter implements AdvancedFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {

    Set<Object> keys = new HashSet<>();
    Set<Object> result = new LinkedHashSet<>();

    Object attr = null;
    if (args.length > 0) {
      attr = args[0];
    }

    ForLoop loop = ObjectIterator.getLoop(attr);
    while (loop.hasNext()) {
      keys.add(loop.next());
    }

    loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object next = loop.next();
      if (keys.contains(next)) {
        result.add(next);
      }
    }

    return new ArrayList<>(result);
  }

  @Override
  public String getName() {
    return "intersect";
  }
}
