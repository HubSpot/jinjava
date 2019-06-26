package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.apache.commons.lang3.StringUtils;


/**
 * trim(value) Strip leading and trailing whitespace.
 */
@JinjavaDoc(
    value = "Strip leading and trailing whitespace.",
    input = @JinjavaParam(value = "string", type = "string", desc = "the string to strip whitespace from", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{{ \" remove whitespace \"|trim }}")
    })
public class TrimFilter implements Filter {

  @Override
  public String getName() {
    return "trim";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return StringUtils.trim(Objects.toString(var));
  }

}
