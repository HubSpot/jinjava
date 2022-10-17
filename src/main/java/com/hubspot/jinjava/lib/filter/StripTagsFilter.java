package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * striptags(value) Strip SGML/XML tags and replace adjacent whitespace by one space.
 */
@JinjavaDoc(
  value = "Strip SGML/XML tags and replace adjacent whitespace by one space.",
  input = @JinjavaParam(
    value = "string",
    desc = "string to strip tags from",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% set some_html = \"<div><strong>Some text</strong></div>\" %}\n" +
      "{{ some_html|striptags }}"
    )
  }
)
public class StripTagsFilter implements Filter {
  private static final Pattern WHITESPACE = Pattern.compile("\\s{2,}");

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (!(object instanceof String)) {
      return object;
    }
    int numDeferredTokensStart = interpreter.getContext().getDeferredTokens().size();

    String val = interpreter.renderFlat((String) object);
    if (interpreter.getContext().getDeferredTokens().size() > numDeferredTokensStart) {
      throw new DeferredValueException("Deferred in StripTagsFilter");
    }

    String cleanedVal = Jsoup.parse(val).text();
    cleanedVal = Jsoup.clean(cleanedVal, Safelist.none());

    // backwards compatibility with Jsoup.parse
    cleanedVal = cleanedVal.replaceAll("&nbsp;", " ");

    String normalizedVal = WHITESPACE.matcher(cleanedVal).replaceAll(" ");

    return normalizedVal;
  }

  @Override
  public String getName() {
    return "striptags";
  }
}
