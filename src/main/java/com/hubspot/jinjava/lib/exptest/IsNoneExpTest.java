package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if the given object is null / none",
    snippets = {
        @JinjavaSnippet(
            code = "{% unless variable is none %}\n" +
                "     <!--code to render unless the variable is null-->\n" +
                "{% endunless %}")
    })
public class IsNoneExpTest implements ExpTest {

  @Override
  public String getName() {
    return "none";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var == null;
  }

}
