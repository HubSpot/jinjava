package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Enforce HTML escaping. This will probably double escape variables.",
    params = @JinjavaParam(value = "value", desc = "Value to escape"),
    snippets = {
        @JinjavaSnippet(
            code = "{% set escape_string = \"<div>This markup is printed as text</div>\" %}\n" +
                "{{ escape_string|forceescape }}\n")
    })
public class ForceEscapeFilter implements Filter {

  @Override
  public String getName() {
    return "forceescape";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return StringEscapeUtils.escapeHtml4(Objects.toString(var, ""));
  }

}
