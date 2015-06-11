package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if the variable is defined",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is defined %}\n" +
                "<!--code to render if variable is defined-->\n" +
                "{% endif %}")
    })
public class IsDefinedExpTest implements ExpTest {

  @Override
  public String getName() {
    return "defined";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null;
  }

}
