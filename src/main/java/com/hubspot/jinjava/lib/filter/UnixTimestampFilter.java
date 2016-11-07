package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;

@JinjavaDoc(
    value = "Gets the UNIX timestamp value (in milliseconds) of a date object",
    params = {
        @JinjavaParam(value = "value", defaultValue = "current time", desc = "The date variable"),
    },
    snippets = {
        @JinjavaSnippet(code = "{% mydatetime|unixtimestamp %}"),
    })
public class UnixTimestampFilter implements Filter {

  @Override
  public String getName() {
    return "unixtimestamp";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return Functions.unixtimestamp(var);
  }

}
