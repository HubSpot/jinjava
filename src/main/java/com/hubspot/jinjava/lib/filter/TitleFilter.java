package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

/**
 * Return a titlecased version of the value. I.e. words will start with uppercase letters, all remaining characters are lowercase.
 *
 * @author jstehler
 */
@JinjavaDoc(
  value = "Return a titlecased version of the value. I.e. words will start with uppercase letters, all remaining characters are lowercase.",
  input = @JinjavaParam(
    value = "string",
    type = "string",
    desc = "the string to titlecase",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{ \"My title should be titlecase\"|title }} ") }
)
public class TitleFilter implements Filter {

  @Override
  public String getName() {
    return "title";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return null;
    }

    String value = var.toString();

    char[] chars = value.toCharArray();
    boolean titleCased = false;

    for (int i = 0; i < chars.length; i++) {
      if (Character.isWhitespace(chars[i])) {
        titleCased = false;
        continue;
      }

      char original = chars[i];
      if (titleCased) {
        chars[i] = Character.toLowerCase(original);
      } else {
        if (charCanBeTitlecased(original)) {
          chars[i] = Character.toTitleCase(original);
          titleCased = true;
        }
      }
    }

    return new String(chars);
  }

  private boolean charCanBeTitlecased(char c) {
    return Character.toLowerCase(c) != Character.toTitleCase(c);
  }
}
