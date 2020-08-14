package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if object is a boolean and true",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if false is true %}\n" +
      "      <!--this code will never render-->\n" +
      "{% endif %}"
    )
  }
)
public class IsTrueExpTest implements ExpTest {

  @Override
  public String getName() {
    return "true";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var instanceof Boolean && (Boolean) var;
  }
}
