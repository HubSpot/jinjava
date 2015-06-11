package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Apply Python string formatting to an object.",
    params = {
        @JinjavaParam(value = "value", desc = "String value to reformat"),
        @JinjavaParam(value = "args", type = "String...", desc = "Values to insert into string")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "%s can be replaced with other variables or values",
            code = "{{ \"Hi %s %s\"|format(contact.firstname, contact.lastname) }} ")
    })
public class FormatFilter implements Filter {

  @Override
  public String getName() {
    return "format";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String fmt = Objects.toString(var, "");
    return String.format(fmt, (Object[]) args);
  }

}
