package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Returns true if a value is within a list",
    input = @JinjavaParam(value = "value", type="object"),
    params = @JinjavaParam(value = "list", type="list", desc = "A list to check if the value is in."),
    snippets = {
        @JinjavaSnippet(
            code = "{{ 2 is within [1, 2, 3] }}")
    })
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
