package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Uses whitespace to center the value in a field of a given width.",
    input = @JinjavaParam(value = "value", desc = "Value to center", required = true),
    params = {
        @JinjavaParam(value = "width", type = "number", defaultValue = "80", desc = "Width of field to center value in")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "Since HubSpot's compiler automatically strips whitespace, this filter will only work in tags where whitespace is retained, such as a <pre>",
            code = "<pre>\n" +
                "    {% set var = \"string to center\" %}\n" +
                "    {{ var|center(80) }}\n" +
                "</pre>")
    })
public class CenterFilter implements Filter {

  @Override
  public String getName() {
    return "center";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {

    if (var == null) {
      return null;
    }

    int size = 80;
    if (args.length > 0) {
      size = NumberUtils.toInt(args[0], 80);
    }

    if (var instanceof String) {
      return StringUtils.center(var.toString(), size);
    }
    return safeFilter(var, interpreter, args);
  }

}
