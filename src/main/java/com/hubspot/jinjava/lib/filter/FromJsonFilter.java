package com.hubspot.jinjava.lib.filter;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Converts JSON string to Object",
    params = {
        @JinjavaParam(value = "s", desc = "JSON String to write to object")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{object|fromJson}}"
        )
    })
public class FromJsonFilter implements Filter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {

      if (var instanceof String) {
        return OBJECT_MAPPER.readValue((String) var, HashMap.class);
      } else {
        throw new InterpretException(String.format("%s filter requires a string parameter", getName()));
      }

    } catch (IOException e) {
      throw new InterpretException("Could not convert JSON string to object in `fromjson` filter.", e, interpreter.getLineNumber());
    }
  }

  @Override
  public String getName() {
    return "fromjson";
  }
}
