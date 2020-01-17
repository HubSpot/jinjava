package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Capitalize a value. The first character will be uppercase, all others lowercase.",
    input = @JinjavaParam(value = "string", desc = "String to capitalize the first letter of", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{% set sentence = \"the first letter of a sentence should always be capitalized.\" %}\n" +
                "{{ sentence|capitalize }}")
    })
public class CapitalizeFilter implements Filter {

  @Override
  public String getName() {
    return "capitalize";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (var == null) {
      return null;
    }

    if (var instanceof String) {
      String value = (String) var;
      return StringUtils.capitalize(value);
    }
    return var;
  }

}
