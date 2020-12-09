package com.hubspot.jinjava.lib.filter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Map;
import java.util.Objects;

/**
 * split(separator=' ', limit=0)
 *
 * Splits the input string into a list on the given separator
 *
 * separator: defaults to space limit: defaults to 0, limits resulting list by putting remainder of string into last list item
 *
 * @author jstehler
 */
@JinjavaDoc(
  value = "Splits the input string into a list on the given separator",
  input = @JinjavaParam(value = "string", desc = "The string to split", required = true),
  params = {
    @JinjavaParam(
      value = SplitFilter.SEPARATOR_PARAM,
      defaultValue = " ",
      desc = "Specifies the separator to split the variable by"
    ),
    @JinjavaParam(
      value = SplitFilter.LIMIT_PARAM,
      type = "int",
      defaultValue = "0",
      desc = "Limits resulting list by putting remainder of string into last list item"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% set string_to_split = \"Stephen; David; Cait; Nancy; Mike; Joe; Niall; Tim; Amanda\" %}\n" +
      "{% set names = string_to_split|split(';', 4) %}\n" +
      "<ul>\n" +
      "   {% for name in names %}\n" +
      "       <li>{{ name }}</li>\n" +
      "   {% endfor %}\n" +
      "</ul>"
    )
  }
)
public class SplitFilter extends AbstractFilter implements Filter {
  public static final String SEPARATOR_PARAM = "separator";
  public static final String LIMIT_PARAM = "limit";

  @Override
  public String getName() {
    return "split";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    String separator = (String) parsedArgs.get(SEPARATOR_PARAM);
    Splitter splitter;
    if (separator != null) {
      splitter = Splitter.on(separator);
    } else {
      splitter = Splitter.on(CharMatcher.whitespace());
    }

    int limit = (Integer) parsedArgs.get(LIMIT_PARAM);
    if (limit > 0) {
      splitter = splitter.limit(limit);
    }

    return Lists.newArrayList(
      splitter.omitEmptyStrings().trimResults().split(Objects.toString(var, ""))
    );
  }
}
