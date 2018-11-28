package com.hubspot.jinjava.lib.filter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;

@JinjavaDoc(
    value = "Calculates the time between two datetime objects",
    params = {
        @JinjavaParam(value = "begin", desc = "Datetime object or timestamp at the beginning of the period"),
        @JinjavaParam(value = "end", desc = "Datetime object or timestamp at the end of the period"),
        @JinjavaParam(value = "unit", desc = "Which temporal unit to use"),
    },
    snippets = {
        @JinjavaSnippet(code = "{% begin|between_times(end, 'hours') %}"),
    })
public class BetweenTimesFilter implements AdvancedFilter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {

    if (args.length < 2) {
      throw new InterpretException(String.format("%s filter requires a datetime and a string parameter", getName()));
    }

    ZonedDateTime start = getZonedDateTime(var);
    ZonedDateTime end = getZonedDateTime(args[0]);

    Object args1 = args[1];
    if (!(args1 instanceof String)) {
      throw new InterpretException(String.format("%s filter requires a string as the second parameter", getName()));
    }

    TemporalUnit temporalUnit = BaseDateFilter.getTemporalUnit((String) args[1]);
    return temporalUnit.between(start, end);
  }

  private ZonedDateTime getZonedDateTime(Object var) {
    if (var instanceof ZonedDateTime) {
      return (ZonedDateTime) var;
    } else if (var instanceof PyishDate) {
      return ((PyishDate) var).toDateTime();
    } else {
      return Functions.getDateTimeArg(var, ZoneOffset.UTC);
    }
  }


  @Override
  public String getName() {
    return "between_times";
  }
}
