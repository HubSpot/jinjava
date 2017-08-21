package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Escapes strings so that they can be used as JSON values",
    params = {
        @JinjavaParam(value = "s", desc = "String to escape")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{String that contains JavaScript|escapejson}}"
        )
    })

public class EscapeJsonFilter implements Filter {

  @Override
  public String getName() {
    return "escapejson";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return StringEscapeUtils.escapeJson(Objects.toString(var));
  }
}
