package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
    value = "Return true if object is a string which starts with a specified other string",
    input = @JinjavaParam(value = "value", type = "string", required = true),
    params = @JinjavaParam(value = "check", type = "string", desc = "A second string to check is the start of the first string", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is string_startingwith 'foo' %}\n" +
                "      <!--code to render if variable starts with 'foo'-->\n" +
                "{% endif %}")
    })
public class IsStringStartingWithExpTest extends IsStringExpTest {

  @Override
  public String getName() {
    return super.getName() + "_startingwith";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (!super.evaluate(var, interpreter, args)) {
      return false;
    }

    if (args.length == 0) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (other string to compare to)");
    }

    if (args[0] == null) {
      return false;
    }

    return ((String) var).startsWith(args[0].toString());
  }

}
