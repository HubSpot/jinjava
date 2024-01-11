package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.lib.filter.time.DateTimeFormatHelper.FIXED_DATE_TIME_FILTER_NULL_ARG;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.features.DateTimeFeatureActivationStrategy;
import com.hubspot.jinjava.features.FeatureActivationStrategy;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;

/**
 * {@link ChronoUnit} for valid time units
 */
@JinjavaDoc(
  value = "Calculates the time between two datetime objects",
  input = @JinjavaParam(
    value = "begin",
    desc = "Datetime object or timestamp at the beginning of the period",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "end",
      desc = "Datetime object or timestamp at the end of the period",
      required = true
    ),
    @JinjavaParam(value = "unit", desc = "Which temporal unit to use", required = true),
  },
  snippets = { @JinjavaSnippet(code = "{{ begin|between_times(end, 'hours') }}") }
)
public class BetweenTimesFilter extends BaseDateFilter {

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    if (args.length < 2) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 datetime (end date) and 1 string (diff unit) argument"
      );
    }

    ZonedDateTime start = getZonedDateTime(var, "begin");
    ZonedDateTime end = getZonedDateTime(args[0], "end");

    Object args1 = args[1];
    if (args1 == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 1);
    }

    TemporalUnit temporalUnit = getTemporalUnit(interpreter, args[1].toString());
    return temporalUnit.between(start, end);
  }

  private ZonedDateTime getZonedDateTime(Object var, String position) {
    if (var instanceof ZonedDateTime) {
      return (ZonedDateTime) var;
    } else if (var instanceof PyishDate) {
      return ((PyishDate) var).toDateTime();
    } else {
      JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

      if (var == null) {
        interpreter.addError(
          TemplateError.fromMissingFilterArgException(
            new InvalidArgumentException(
              interpreter,
              getName() + " filter called with null " + position,
              getName()
            )
          )
        );

        FeatureActivationStrategy feat = interpreter
          .getConfig()
          .getFeatures()
          .getActivationStrategy(FIXED_DATE_TIME_FILTER_NULL_ARG);

        if (feat.isActive(interpreter.getContext())) {
          var = ((DateTimeFeatureActivationStrategy) feat).getActivateAt();
        }
      }

      return Functions.getDateTimeArg(var, ZoneOffset.UTC);
    }
  }

  @Override
  public String getName() {
    return "between_times";
  }
}
