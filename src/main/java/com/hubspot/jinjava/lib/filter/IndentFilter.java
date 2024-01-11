package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Uses whitespace to indent a string.",
  input = @JinjavaParam(value = "string", desc = "The string to indent", required = true),
  params = {
    @JinjavaParam(
      value = IndentFilter.WIDTH_PARAM,
      type = "number",
      defaultValue = "4",
      desc = "Amount of whitespace to indent"
    ),
    @JinjavaParam(
      value = IndentFilter.INDENT_FIRST_PARAM,
      type = "boolean",
      defaultValue = "False",
      desc = "If True, first line will be indented"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "Since HubSpot's compiler automatically strips whitespace, this filter will only work in tags where whitespace is retained, such as a <pre>",
      code = "<pre>\n" +
      "    {% set var = \"string to indent\" %}\n" +
      "    {{ var|indent(2, true) }}\n" +
      "</pre>"
    ),
  }
)
public class IndentFilter extends AbstractFilter {

  public static final String INDENT_FIRST_PARAM = "indentfirst";
  public static final String WIDTH_PARAM = "width";

  @Override
  public String getName() {
    return "indent";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    int width = ((Number) parsedArgs.get(WIDTH_PARAM)).intValue();

    boolean indentFirst = (boolean) parsedArgs.get(INDENT_FIRST_PARAM);

    List<String> indentedLines = new ArrayList<>();
    for (String line : NEWLINE_SPLITTER.split(Objects.toString(var, ""))) {
      int thisWidth = indentedLines.size() == 0 && !indentFirst ? 0 : width;
      indentedLines.add(StringUtils.repeat(' ', thisWidth) + line);
    }

    return NEWLINE_JOINER.join(indentedLines);
  }

  private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
  private static final Joiner NEWLINE_JOINER = Joiner.on('\n');
}
