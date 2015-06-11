package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if the variable is a sequence. Sequences are variables that are iterable.",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is sequence %}\n" +
                "      <!--code to render if items in a variable is a sequence-->\n" +
                "{% endif %}")
    })
public class IsSequenceExpTest implements ExpTest {

  @Override
  public String getName() {
    return "sequence";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null && (var.getClass().isArray() || Iterable.class.isAssignableFrom(var.getClass()));
  }

}
