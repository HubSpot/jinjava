package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if object is an integer or long",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if num is integer %}\n" +
      "      <!--code to render if num contains an integral value-->\n" +
      "{% endif %}"
    )
  }
)
public class IsIntegerExpTest implements ExpTest {

  @Override
  public String getName() {
    return "integer";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var instanceof Integer || var instanceof Long;
  }
}
