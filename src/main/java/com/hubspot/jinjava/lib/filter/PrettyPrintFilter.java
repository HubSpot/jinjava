package com.hubspot.jinjava.lib.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
    ),
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
      var instanceof Iterable
    ) {
      varStr = Objects.toString(var);
    } else if (var instanceof Map) {
      TreeMap map = new TreeMap((Map) var);
      varStr = Objects.toString(map);
    } else {
      try {
        varStr =
          interpreter
            .getConfig()
            .getObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(var);
      } catch (JsonProcessingException e) {
        throw new InvalidInputException(interpreter, this, InvalidReason.JSON_WRITE);
      }
    }

    return EscapeFilter.escapeHtmlEntities(
      "{% raw %}(" + var.getClass().getSimpleName() + ": " + varStr + "){% endraw %}"
    );
  }
}
