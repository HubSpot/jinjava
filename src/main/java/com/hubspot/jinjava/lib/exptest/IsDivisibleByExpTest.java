package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
  value = "Returns true if a variable is divisible by a number",
  input = @JinjavaParam(value = "num", type = "number", required = true),
  params = @JinjavaParam(
    value = "divisor",
    type = "number",
    desc = "The number to check whether a number is divisible by",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is divisibleby 5 %}\n" +
      "   <!--code to render if variable can be divided by 5-->\n" +
      "{% else %}\n" +
      "   <!--code to render if variable cannot be divided by 5-->\n" +
      "{% endif %}"
    ),
  }
)
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
    Number freeFormDividend = (Number) var;
    if (
      Math.ceil(freeFormDividend.doubleValue()) !=
      Math.floor(freeFormDividend.doubleValue())
    ) {
      return false;
    }
    int dividend = freeFormDividend.intValue();

    if (args.length == 0) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (name of expression test to filter by)"
      );
    }

    if (args[0] == null) {
      return false;
    }

    if (!Number.class.isAssignableFrom(args[0].getClass())) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NUMBER_FORMAT,
        0,
        args[0].toString()
      );
    }

    Number freeFormDivisor = (Number) args[0];
    if (
      Math.floor(freeFormDivisor.doubleValue()) !=
      Math.ceil(freeFormDivisor.doubleValue())
    ) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NON_ZERO_NUMBER,
        0,
        args[0].toString()
      );
    }

    int divisor = ((Number) args[0]).intValue();
    if (divisor == 0) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NON_ZERO_NUMBER,
        0,
        args[0].toString()
      );
    }

    return (dividend % divisor) == 0;
  }
}
