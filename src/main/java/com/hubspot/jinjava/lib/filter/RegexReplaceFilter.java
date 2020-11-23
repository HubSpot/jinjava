package com.hubspot.jinjava.lib.filter;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Map;

@JinjavaDoc(
  value = "Return a copy of the value with all occurrences of a matched regular expression (Java RE2 syntax) " +
  "replaced with a new one. The first argument is the regular expression to be matched, the second " +
  "is the replacement string",
  input = @JinjavaParam(
    value = "s",
    desc = "Base string to find and replace within",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = RegexReplaceFilter.REGEX_KEY,
      desc = "The regular expression that you want to match and replace",
      required = true
    ),
    @JinjavaParam(
      value = RegexReplaceFilter.REPLACE_WITH,
      desc = "The new string that you replace the matched substring",
      required = true
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"It costs $300\"|regex_replace(\"[^a-zA-Z]\", \"\") }}",
      output = "Itcosts"
    )
  }
)
public class RegexReplaceFilter extends AbstractFilter {
  public static final String REGEX_KEY = "regex";
  public static final String REPLACE_WITH = "new";

  @Override
  public String getName() {
    return "regex_replace";
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

    if (var instanceof String) {
      String s = (String) var;
      String toReplace = (String) parsedArgs.get(REGEX_KEY);
      String replaceWith = (String) parsedArgs.get(REPLACE_WITH);

      try {
        Pattern p = Pattern.compile(toReplace);
        Matcher matcher = p.matcher(s);

        return matcher.replaceAll(replaceWith);
      } catch (PatternSyntaxException e) {
        throw new InvalidArgumentException(
          interpreter,
          this,
          InvalidReason.REGEX,
          0,
          toReplace
        );
      }
    } else {
      throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
    }
  }
}
