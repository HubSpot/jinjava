package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(value = "Check if a variable is divisible by a number",
    params = {
        @JinjavaParam(value = "num", type = "number", desc = "The number to check whether a number is divisble by")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% if variable is divisbleby 5 %}\n" +
                "   <!--code to render if variable can be divided by 5-->\n" +
                "{% else %}\n" +
                "   <!--code to render if variable cannot be divided by 5-->\n" +
                "{% endif %}")
    })
public class IsDivisibleByExpTest implements ExpTest {

  @Override
  public String getName() {
    return "divisibleby";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (var == null) {
      return false;
    }
    if (!Number.class.isAssignableFrom(var.getClass())) {
      return false;
    }

    if (args.length == 0) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (name of expression test to filter by)");
    }

    if (args[0] == null) {
      return false;
    }

    if (!Number.class.isAssignableFrom(args[0].getClass())) {
      throw new InvalidArgumentException(interpreter,
          getName(),
          String.format(InvalidArgumentException.NUMBER_FORMAT_EXCEPTION_TEMPLATE, "num", args[0].toString()),
          "num");
    }

    return ((Number) var).intValue() % ((Number) args[0]).intValue() == 0;
  }

}
