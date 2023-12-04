package com.hubspot.jinjava.lib.filter;

import com.fasterxml.jackson.databind.node.POJONode;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.util.Map;
import java.util.Objects;

@JinjavaDoc(
  value = "Pretty print a variable. Useful for debugging.",
  input = @JinjavaParam(
    value = "value",
    type = "object",
    desc = "Object to Pretty Print",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% set this_var =\"Variable that I want to debug\" %}\n" +
      "{{ this_var|pprint }}"
    )
  }
)
public class PrettyPrintFilter implements Filter {

  @Override
  public String getName() {
    return "pprint";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return "null";
    }

    String varStr;

    if (
      var instanceof String ||
      var instanceof Number ||
      var instanceof PyishDate ||
      var instanceof Iterable ||
      var instanceof Map
    ) {
      varStr = Objects.toString(var);
    } else {
      varStr = new POJONode(var).toPrettyString();
    }

    return EscapeFilter.escapeHtmlEntities(
      "{% raw %}(" + var.getClass().getSimpleName() + ": " + varStr + "){% endraw %}"
    );
  }
}
