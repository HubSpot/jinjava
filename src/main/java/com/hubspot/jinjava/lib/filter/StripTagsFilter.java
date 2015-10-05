package com.hubspot.jinjava.lib.filter;

import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

/**
 * striptags(value) Strip SGML/XML tags and replace adjacent whitespace by one space.
 */
@JinjavaDoc(
    value = "Strip SGML/XML tags and replace adjacent whitespace by one space.",
    snippets = {
        @JinjavaSnippet(
            code = "{% set some_html = \"<div><strong>Some text</strong></div>\" %}\n" +
                "{{ some_html|striptags }}")
    })
public class StripTagsFilter implements Filter {

  private static final Pattern WHITESPACE = Pattern.compile("\\s{2,}");

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (!(object instanceof String)) {
      return object;
    }

    String val = interpreter.renderFlat((String) object);
    String strippedVal = Jsoup.parseBodyFragment(val).text();
    String normalizedVal = WHITESPACE.matcher(strippedVal).replaceAll(" ");

    return normalizedVal;
  }

  @Override
  public String getName() {
    return "striptags";
  }

}
