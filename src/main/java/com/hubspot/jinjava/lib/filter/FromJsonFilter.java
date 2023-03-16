package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.IOException;

@JinjavaDoc(
  value = "Converts JSON string to Object",
  input = @JinjavaParam(
    value = "string",
    desc = "JSON String to write to object",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{object|fromJson}}") }
)
public class FromJsonFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return null;
    }

    if (!(var instanceof String)) {
      throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
    }
    try {
      return interpreter
        .getConfig()
        .getObjectMapper()
        .readValue((String) var, Object.class);
    } catch (IOException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_READ);
    }
  }

  @Override
  public String getName() {
    return "fromjson";
  }
}
