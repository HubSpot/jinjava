package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;

@JinjavaDoc(
    value = "Converts a datetime string and datetime format to a datetime object",
    params = {
        @JinjavaParam(value = "datetimeString", desc = "Datetime string"),
        @JinjavaParam(value = "datetimeFormat", desc = "Format of the datetime string"),
    },
    snippets = {
        @JinjavaSnippet(code = "{% mydatetime|unixtimestamp %}"),
    })
public class StringToTimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {

    if (!(var instanceof String)) {
      throw new InterpretException(String.format("%s filter requires a string as input", getName()));
    }

    if (args.length < 1) {
      throw new InterpretException(String.format("%s filter requires a datetime format parameter", getName()));
    }

    return Functions.stringToTime((String) var, args[0]);
  }

  @Override
  public String getName() {
    return "strtotime";
  }
}
