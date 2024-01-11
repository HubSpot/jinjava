package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if object is a boolean and false",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if false is false %}\n" +
      "      <!--this code will always render-->\n" +
      "{% endif %}"
    ),
  }
)
public class IsFalseExpTest implements ExpTest {

  @Override
  public String getName() {
    return "false";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var instanceof Boolean && !(Boolean) var;
  }
}
