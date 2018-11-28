package com.hubspot.jinjava.lib.filter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;

@JinjavaDoc(
    value = "Subtracts a specified amount of time to a datetime object",
    params = {
        @JinjavaParam(value = "var", desc = "Datetime object or timestamp"),
        @JinjavaParam(value = "diff", desc = "The amount to subtract from the datetime"),
        @JinjavaParam(value = "unit", desc = "Which temporal unit to use"),
    },
    snippets = {
        @JinjavaSnippet(code = "{% mydatetime|minus_time(3, 'days') %}"),
    })
public class MinusTimeFilter extends BaseDateFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {

    TemporalAmount diff = parseArgs(args);
    if (var instanceof ZonedDateTime) {
      ZonedDateTime dateTime = (ZonedDateTime) var;
      return dateTime.minus(diff);
    } else if (var instanceof PyishDate) {
      PyishDate pyishDate = (PyishDate) var;
      return new PyishDate(pyishDate.toDateTime().minus(diff));
    } else if (var instanceof Long) {
      Long timestamp = (Long) var;
      ZonedDateTime zonedDateTime = Functions.getDateTimeArg(timestamp, ZoneOffset.UTC);
      return new PyishDate(zonedDateTime.minus(diff));
    }

    return var;
  }

  @Override
  public String getName() {
    return "minus_time";
  }
}
