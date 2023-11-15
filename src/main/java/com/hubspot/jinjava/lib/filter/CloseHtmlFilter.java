package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@JinjavaDoc(
  value = "Closes open HTML tags in a string",
  input = @JinjavaParam(value = "s", desc = "String to close", required = true),
  snippets = { @JinjavaSnippet(code = "{{ \"<p> Hello, world\"|closehtml }}") }
)
public class CloseHtmlFilter implements Filter {

  @Override
  public String getName() {
    return "closehtml";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return Jsoup.parseBodyFragment(Objects.toString(var)).body().html();
  }
}
