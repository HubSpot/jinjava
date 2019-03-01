package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Returns true if a list contains a value",
    input = @JinjavaParam(value = "list", type = "list", required = true),
    params = @JinjavaParam(value = "value", type = "object", desc = "The value to check is in the list", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3] is containing 2 }}")
    })
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
