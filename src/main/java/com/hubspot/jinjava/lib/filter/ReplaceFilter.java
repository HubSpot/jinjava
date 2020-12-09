package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Return a copy of the value with all occurrences of a substring replaced with a new one. " +
  "The first argument is the substring that should be replaced, the second is the replacement " +
  "string. If the optional third argument count is given, only the first count occurrences are replaced",
  input = @JinjavaParam(
    value = "s",
    desc = "Base string to find and replace within",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = ReplaceFilter.OLD_KEY,
      desc = "The old substring that you want to match and replace",
      required = true
    ),
    @JinjavaParam(
      value = ReplaceFilter.REPLACE_WITH_KEY,
      desc = "The new string that you replace the matched substring",
      required = true
    ),
    @JinjavaParam(
      value = ReplaceFilter.COUNT_KEY,
      type = "int",
      desc = "Replace only the first N occurrences"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"Hello World\"|replace(\"Hello\", \"Goodbye\") }}",
      output = "Goodbye World"
    ),
    @JinjavaSnippet(
      code = "{{ \"aaaaargh\"|replace(\"a\", \"d'oh, \", 2) }}",
      output = "d'oh, d'oh, aaargh"
    )
  }
)
public class ReplaceFilter extends AbstractFilter {
  public static final String OLD_KEY = "old";
  public static final String REPLACE_WITH_KEY = "new";
  public static final String COUNT_KEY = "count";

  @Override
  public String getName() {
    return "replace";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    if (var == null) {
      return null;
    }

    String s = (String) var;
    String toReplace = (String) parsedArgs.get(OLD_KEY);
    String replaceWith = (String) parsedArgs.get(REPLACE_WITH_KEY);
    Integer count = (Integer) (parsedArgs.get(COUNT_KEY));

    if (count == null) {
      return StringUtils.replace(s, toReplace, replaceWith);
    } else {
      return StringUtils.replace(s, toReplace, replaceWith, count);
    }
  }
}
