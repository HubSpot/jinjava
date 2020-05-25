package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if object is a boolean (in a strict sense, not in its ability to evaluate to a truthy expression)",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if true is boolean %}\n" +
      "      <!--this code will always render-->\n" +
      "{% endif %}"
    )
  }
)
public class IsBooleanExpTest implements ExpTest {

  @Override
  public String getName() {
    return "boolean";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var instanceof Boolean;
  }
}
