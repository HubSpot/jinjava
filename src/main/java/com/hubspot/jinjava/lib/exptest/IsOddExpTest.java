package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if the object is an odd number",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is odd %}\n" +
                "   <!--code to render if variable is an odd number-->\n" +
                "{% else %}\n" +
                "   <!--code to render if variable is an even number-->\n" +
                "{% endif %}")
    })
public class IsOddExpTest implements ExpTest {

  @Override
  public String getName() {
    return "odd";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if (var == null || !Number.class.isAssignableFrom(var.getClass())) {
      return false;
    }

    return ((Number) var).intValue() % 2 != 0;
  }

}
