package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return true if object is a string which starts with a specified other string",
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

    if (args.length == 0 || args[0] == null) {
      throw new InterpretException(getName() + " test requires an argument");
    }

    return ((String) var).startsWith(args[0].toString());
  }

}
