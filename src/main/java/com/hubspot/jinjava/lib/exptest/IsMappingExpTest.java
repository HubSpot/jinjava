package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Map;

@JinjavaDoc(
  value = "Return true if the given object is a dict",
  input = @JinjavaParam(value = "object", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is mapping %}\n" +
      "     <!--code to render when object is a dict-->\n" +
      "{% endif %}"
    ),
  }
)
public class IsMappingExpTest implements ExpTest {

  @Override
  public String getName() {
    return "mapping";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var != null && Map.class.isAssignableFrom(var.getClass());
  }
}
