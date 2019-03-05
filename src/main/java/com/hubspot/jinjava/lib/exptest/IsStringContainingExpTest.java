package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
    value = "Return true if object is a string which contains a specified other string",
    input =  @JinjavaParam(value = "string", type = "string", required = true),
    params = @JinjavaParam(value = "check", type = "string", desc = "A second string to check is contained in the first string", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is string_containing 'foo' %}\n" +
                "      <!--code to render if variable contains 'foo' -->\n" +
                "{% endif %}")
    })
public class IsStringContainingExpTest extends IsStringExpTest {

  @Override
  public String getName() {
    return super.getName() + "_containing";
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

    return ((String) var).contains(args[0].toString());
  }

}
