package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Return a string which is the concatenation of the strings in the sequence.",
    params = {
        @JinjavaParam(value = "value", desc = "The values to join"),
        @JinjavaParam(value = "d", desc = "The separator string used to join the items"),
        @JinjavaParam(value = "attr", desc = "Optional dict object attribute to use in joining")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3]|join('|') }}",
            output = "1|2|3"),
        @JinjavaSnippet(
            code = "{{ [1, 2, 3]|join }}",
            output = "123"),
        @JinjavaSnippet(
            desc = "It is also possible to join certain attributes of an object",
            code = "{{ users|join(', ', attribute='username') }}")
    })
public class JoinFilter implements Filter {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<String> vals = new ArrayList<>();

    String separator = "";
    if (args.length > 0) {
      separator = args[0];
    }

    String attr = null;
    if (args.length > 1) {
      attr = args[1];
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();

      if (attr != null) {
        val = interpreter.resolveProperty(val, attr);
      }

      vals.add(Objects.toString(val, ""));
    }

    return Joiner.on(separator).join(vals);
  }

}
