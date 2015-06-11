package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Return the last item of a sequence",
    params = {
        @JinjavaParam(value = "seq", type = "sequence", desc = "Sequence to return last item from")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_sequence = ['Item 1', 'Item 2', 'Item 3'] %}\n" +
                "{{ my_sequence|last }}")
    })
public class LastFilter implements Filter {

  @Override
  public String getName() {
    return "last";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    Object last = null;

    while (loop.hasNext()) {
      last = loop.next();
    }

    return last;
  }

}
