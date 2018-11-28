package com.hubspot.jinjava.lib.filter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;

public class PlusTimeFilter extends BaseDateFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {

    TemporalAmount diff = parseArgs(args);
    if (var instanceof ZonedDateTime) {
      ZonedDateTime dateTime = (ZonedDateTime) var;
      return new PyishDate(dateTime.plus(diff));
    } else if (var instanceof PyishDate) {
      PyishDate pyishDate = (PyishDate) var;
      return new PyishDate(pyishDate.toDateTime().plus(diff));
    } else if (var instanceof Number) {
      Number timestamp = (Number) var;
      ZonedDateTime zonedDateTime = Functions.getDateTimeArg(timestamp, ZoneOffset.UTC);
      return new PyishDate(zonedDateTime.plus(diff));
    }

    return var;
  }

  @Override
  public String getName() {
    return "plus_time";
  }
}
