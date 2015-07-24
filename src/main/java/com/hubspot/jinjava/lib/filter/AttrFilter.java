package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Renders the attribute of a dictionary",
    params = {
        @JinjavaParam(value = "obj", desc = "The dictionary containing the attribute"),
        @JinjavaParam(value = "name", desc = "The dictionary attribute name to access")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "The filter example below is equivalent to rendering a variable that exists within a dictionary, such as content.absolute_url.",
            code = "{{ content|attr('absolute_url') }}")
    })
public class AttrFilter implements Filter {

  @Override
  public String getName() {
    return "attr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length == 0) {
      throw new InterpretException(getName() + " requires an attr name to use", interpreter.getLineNumber());
    }

    return interpreter.resolveProperty(var, args[0]);
  }

}
