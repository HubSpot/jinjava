package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if object is a string",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is string %}\n" +
                "      <!--code to render if a variable contains a string value-->\n" +
                "{% endif %}")
    })
public class IsStringExpTest implements ExpTest {

  @Override
  public String getName() {
    return "string";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var != null && var instanceof String;
  }

}
