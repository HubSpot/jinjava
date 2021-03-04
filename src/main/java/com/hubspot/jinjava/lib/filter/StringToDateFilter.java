package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.Functions;

public class StringToDateFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (date format string)"
      );
    }

    if (var == null) {
      return null;
    }

    if (!(var instanceof String)) {
      throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
    }

    return Functions.stringToDate((String) var, args[0]);
  }

  @Override
  public String getName() {
    return "strtodate";
  }
}
