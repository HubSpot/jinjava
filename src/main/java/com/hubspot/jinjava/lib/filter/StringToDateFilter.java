package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.Functions;

@JinjavaDoc(
  value = "Converts a date string and date format to a date object",
  input = @JinjavaParam(value = "dateString", desc = "Date string", required = true),
  params = {
    @JinjavaParam(
      value = "dateFormat",
      desc = "Format of the date string",
      required = true
    )
  },
  snippets = { @JinjavaSnippet(code = "{{ '3/3/21'|strtodate('M/d/yy') }}") }
)
public class StringToDateFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (date format string)"
      );
    }

    if (var == null) {
      return null;
    }

    if (!(var instanceof String)) {
      var = String.valueOf(var);
    }

    return Functions.stringToDate((String) var, args[0]);
  }

  @Override
  public String getName() {
    return "strtodate";
  }
}
