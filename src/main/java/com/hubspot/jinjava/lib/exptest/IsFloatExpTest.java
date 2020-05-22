package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if object is a float",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if num is float %}\n" +
      "      <!--code to render if num contains an floating point value-->\n" +
      "{% endif %}"
    )
  }
)
public class IsFloatExpTest implements ExpTest {

  @Override
  public String getName() {
    return "float";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null && (var instanceof Double || var instanceof Float);
  }
}
