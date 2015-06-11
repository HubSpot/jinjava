package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if object is undefined",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is undefined %}\n" +
                "      <!--code to render if variable is undefined-->\n" +
                "{% endif %}")
    })
public class IsUndefinedExpTest implements ExpTest {

  @Override
  public String getName() {
    return "undefined";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    return var == null;
  }

}
