package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * {@link ChronoUnit} for valid time units
 */
@JinjavaDoc(
    value = "Subtracts a specified amount of time to a datetime object",
    input = @JinjavaParam(value = "var", desc = "Datetime object or timestamp", required = true),
    params = {
        @JinjavaParam(value = "diff", desc = "The amount to subtract from the datetime", required = true),
        @JinjavaParam(value = "unit", desc = "Which temporal unit to use", required = true),
    },
    snippets = {
        @JinjavaSnippet(code = "{% mydatetime|minus_time(3, 'days') %}"),
    })
public class MinusTimeFilter extends BaseDateFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return null;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    long diff = parseDiffAmount(interpreter, args);
    ChronoUnit chronoUnit = parseChronoUnit(interpreter, args);

    if (var instanceof ZonedDateTime) {
      ZonedDateTime dateTime = (ZonedDateTime) var;
      return new PyishDate(dateTime.minus(diff, chronoUnit));
    } else if (var instanceof PyishDate) {
      PyishDate pyishDate = (PyishDate) var;
      return new PyishDate(pyishDate.toDateTime().minus(diff, chronoUnit));
    } else if (var instanceof Number) {
      Number timestamp = (Number) var;
      ZonedDateTime zonedDateTime = Functions.getDateTimeArg(timestamp, ZoneOffset.UTC);
      return new PyishDate(zonedDateTime.minus(diff, chronoUnit));
    }

    return var;
  }

  @Override
  public String getName() {
    return "minus_time";
  }
}
