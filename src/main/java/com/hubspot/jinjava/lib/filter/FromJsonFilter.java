package com.hubspot.jinjava.lib.filter;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Converts JSON string to Object",
    input = @JinjavaParam(value = "string", desc = "JSON String to write to object"),
    snippets = {
        @JinjavaSnippet(
            code = "{{object|fromJson}}"
        )
    })
public class FromJsonFilter implements Filter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {

    if (var == null) {
      return null;
    }

    try {

      if (var instanceof String) {
        return OBJECT_MAPPER.readValue((String) var, HashMap.class);
      } else {
        throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
      }
    } catch (IOException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_READ);
    }
  }

  @Override
  public String getName() {
    return "fromjson";
  }
}
