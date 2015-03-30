package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;

@JinjavaDoc(
    value="format a date object",
    params={
        @JinjavaParam(value="value", defaultValue="current time"),
        @JinjavaParam(value="format", defaultValue=StrftimeFormatter.DEFAULT_DATE_FORMAT)
    })
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
