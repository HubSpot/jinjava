package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if the object is a number",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is number %}\n" +
                "       {{ my_var * 1000000 }}\n" +
                "{% else %}\n" +
                "       The variable is not a number.\n" +
                "{% endif %}")
    })
public class IsNumberExpTest implements ExpTest {

  @Override
  public String getName() {
    return "number";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var != null && Number.class.isAssignableFrom(var.getClass());
  }

}
