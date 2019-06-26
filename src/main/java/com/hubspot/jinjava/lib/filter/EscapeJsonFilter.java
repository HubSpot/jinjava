package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.apache.commons.lang3.StringEscapeUtils;


@JinjavaDoc(
    value = "Escapes strings so that they can be used as JSON values",
    input = @JinjavaParam(value = "s", desc = "String to escape", required = true),
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
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return StringEscapeUtils.escapeJson(Objects.toString(var));
  }

}
