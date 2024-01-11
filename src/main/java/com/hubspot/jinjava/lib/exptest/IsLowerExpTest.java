package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Return true if the given string is all lowercase",
  input = @JinjavaParam(value = "string", type = "string", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is lower %}\n" +
      "   <!--code to render if variable value is lowercased-->\n" +
      "{% endif %}"
    ),
  }
)
public class IsLowerExpTest implements ExpTest {

  @Override
  public String getName() {
    return "lower";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (var == null || !(var instanceof String)) {
      return false;
    }

    return StringUtils.isAllLowerCase((String) var);
  }
}
