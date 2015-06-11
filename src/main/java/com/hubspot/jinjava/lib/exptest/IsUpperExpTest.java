package com.hubspot.jinjava.lib.exptest;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if string is all uppercased",
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is upper %}\n" +
                "    <!-- code to render if variable value is uppercased -->\n" +
                "{% endif %}")
    })
public class IsUpperExpTest implements ExpTest {

  @Override
  public String getName() {
    return "upper";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if (var == null || !(var instanceof String)) {
      return false;
    }

    return StringUtils.isAllUpperCase((String) var);
  }

}
