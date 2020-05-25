package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

@JinjavaDoc(
  value = "Return true if the object is marked as escaped.",
  input = @JinjavaParam(value = "object", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if 'test' is escaped %}\n" +
      "      <!--this code will not render-->\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      code = "{% if ('test'|escape) is escaped %}\n" +
      "      <!--this code will render-->\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      code = "{% if ('test'|safe) is escaped %}\n" +
      "      <!--this code will render-->\n" +
      "{% endif %}"
    )
  }
)
public class IsEscapedExpTest implements ExpTest {

  @Override
  public String getName() {
    return "escaped";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return var instanceof SafeString;
  }
}
