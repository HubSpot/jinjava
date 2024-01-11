package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Return true if the object is iterable (sequence, dict, etc)",
  input = @JinjavaParam(value = "object", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is iterable %}\n" +
      "       <!--code to render if items in a variable can be iterated through-->\n" +
      "{% endif %}"
    ),
  }
)
public class IsIterableExpTest implements ExpTest {

  @Override
  public String getName() {
    return "iterable";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return isIterable(var);
  }

  static boolean isIterable(Object ob) {
    return (
      ob != null &&
      (ob.getClass().isArray() || Iterable.class.isAssignableFrom(ob.getClass()))
    );
  }
}
