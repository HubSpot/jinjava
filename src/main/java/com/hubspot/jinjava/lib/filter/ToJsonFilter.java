package com.hubspot.jinjava.lib.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc(
    value = "Writes objects as JSON strings",
    params = {
        @JinjavaParam(value = "o", desc = "Object to write to JSON")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{object|escapejson}}"
        )
    })
public class ToJsonFilter implements Filter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {
      return OBJECT_MAPPER.writeValueAsString(var);
    } catch (JsonProcessingException e) {
      throw new InterpretException("Could not write object as string for `escapejson` filter.", e, interpreter.getLineNumber());
    }
  }

  @Override
  public String getName() {
    return "tojson";
  }
}
