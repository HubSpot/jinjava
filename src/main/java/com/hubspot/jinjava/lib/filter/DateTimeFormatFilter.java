package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;

public class DateTimeFormatFilter implements Filter {

  @Override
  public String getName() {
    return "datetimeformat";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter,
      String... args) {

    if(args.length > 0) {
      return Functions.dateTimeFormat(var, args[0]);
    }
    else {
      return Functions.dateTimeFormat(var);
    }

  }

}
