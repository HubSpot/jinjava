package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Return true if string is all uppercased",
  input = @JinjavaParam(value = "value", type = "string", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is upper %}\n" +
      "    <!-- code to render if variable value is uppercased -->\n" +
      "{% endif %}"
    )
  }
)
public class IsUpperExpTest implements ExpTest {

  @Override
  public String getName() {
    return "upper";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (!(var instanceof String || var instanceof SafeString)) {
      return false;
    }

    return StringUtils.isAllUpperCase(var.toString());
  }
}
