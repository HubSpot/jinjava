package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Return the first item of a sequence.",
    input = @JinjavaParam(value = "seq", type = "sequence", desc = "Sequence to return first item from", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_sequence = ['Item 1', 'Item 2', 'Item 3'] %}\n" +
                "{{ my_sequence|first }}")
    })
public class FirstFilter implements Filter {

  @Override
  public String getName() {
    return "first";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    return loop.next();
  }

}
