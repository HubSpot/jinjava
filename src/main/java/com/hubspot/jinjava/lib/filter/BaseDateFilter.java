package com.hubspot.jinjava.lib.filter;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.primitives.Longs;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

public abstract class BaseDateFilter implements AdvancedFilter {

  private static final Map<String, ChronoUnit> unitMap = Arrays.stream(ChronoUnit.values())
      .collect(Collectors.toMap(u -> u.toString().toLowerCase(), u -> u));

  protected long parseDiffAmount(JinjavaInterpreter interpreter, Object... args) {

    if (args.length < 2) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 number (diff amount) and 1 string (diff unit) argument");
    }

    Object firstArg = args[0];
    if (firstArg == null) {
      firstArg = 0;
    }

    Long diff = Longs.tryParse(firstArg.toString());
    if (diff == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NUMBER_FORMAT, 0, firstArg.toString());
    }
    return diff;
  }

  protected ChronoUnit parseChronoUnit(JinjavaInterpreter interpreter, Object... args) {

    if (args.length < 2) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 number (diff amount) and 1 string (diff unit) argument");
    }

    Object unitString = args[1];
    if (unitString == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 1);
    }

    return getTemporalUnit(interpreter, unitString.toString());
  }

  protected ChronoUnit getTemporalUnit(JinjavaInterpreter interpreter, String temporalUnit) {

    String lowercase = temporalUnit.toLowerCase();
    if (!unitMap.containsKey(lowercase)) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.TEMPORAL_UNIT, 1, lowercase);
    }
    return unitMap.get(lowercase);
  }
}
