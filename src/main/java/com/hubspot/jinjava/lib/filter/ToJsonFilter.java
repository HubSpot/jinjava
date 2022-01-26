package com.hubspot.jinjava.lib.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Writes object as a JSON string",
  input = @JinjavaParam(
    value = "object",
    desc = "Object to write to JSON",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{object|tojson}}") }
)
public class ToJsonFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {
      return interpreter.getConfig().getObjectMapper().writeValueAsString(var);
    } catch (JsonProcessingException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_WRITE);
    }
  }

  @Override
  public String getName() {
    return "tojson";
  }
}
