package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Objects;

@JinjavaDoc(
  value = "Renders a template string early to be used by other filters and functions",
  input = @JinjavaParam(value = "s", desc = "String to render", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"{% if my_val %}} Hello {% else %} world {% endif %}\"|render }}"
    ),
  }
)
public class RenderFilter implements Filter {

  @Override
  public String getName() {
    return "render";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return interpreter.render(Objects.toString(var));
  }
}
