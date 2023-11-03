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
      code = "{{ \"{% if my_val %} Hello {% else %} world {% endif %}\"|render }}"
    )
  }
)
public class RenderFilter implements Filter {

  @Override
  public String getName() {
    return "render";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length > 0) {
      /*
       This means a render limit length has been provided.
       Here we begin a left to right render where we add to an HTML string until the length reaches a certain limit.
       */
    }
    return interpreter.render(Objects.toString(var));
  }
}
