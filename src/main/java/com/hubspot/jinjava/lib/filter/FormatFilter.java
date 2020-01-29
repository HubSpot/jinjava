package com.hubspot.jinjava.lib.filter;

import java.util.Map;
import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

@JinjavaDoc(
    value = "Apply Python string formatting to an object.",
    input = @JinjavaParam(value = "value", desc = "String value to reformat", required = true),
    params = {
        @JinjavaParam(value = "args", type = "String...", desc = "Values to insert into string")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "%s can be replaced with other variables or values",
            code = "{{ \"Hi %s %s\"|format(contact.firstname, contact.lastname) }} ")
    })
public class FormatFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "format";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    if (var instanceof SafeString) {
      return new SafeString(format(var, args));
    }
    return format(var, args);
  }

  private String format(Object var, Object[] args) {
    String fmt = Objects.toString(var, "");
    return String.format(fmt, args);
  }
}
