package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
  value = "Return true if variable is pointing at same object as other variable",
  input = @JinjavaParam(value = "object", type = "object", required = true),
  params = @JinjavaParam(
    value = "other",
    type = "object",
    desc = "A second object to check the variables value against",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% if var_one is sameas var_two %}\n" +
      "    <!--code to render if variables have the same value as one another-->\n" +
      "{% endif %}"
    ),
  }
)
public class IsSameAsExpTest implements ExpTest {

  @Override
  public String getName() {
    return "sameas";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (args.length == 0) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (other object to check the variables value against)"
      );
    }

    return var == args[0];
  }
}
