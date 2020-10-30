package com.hubspot.jinjava.lib.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Writes object as a YAML string",
  input = @JinjavaParam(
    value = "object",
    desc = "Object to write to YAML",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{object|toyaml}}") }
)
public class ToYamlFilter implements Filter {
  private static final YAMLMapper OBJECT_MAPPER = new YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {
      return OBJECT_MAPPER.writeValueAsString(var);
    } catch (JsonProcessingException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_WRITE);
    }
  }

  @Override
  public String getName() {
    return "toyaml";
  }
}
