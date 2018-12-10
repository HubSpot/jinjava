package com.hubspot.jinjava.lib.filter;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.primitives.Longs;
import com.hubspot.jinjava.interpret.InterpretException;

public abstract class BaseDateFilter implements Filter {

  private static final Map<String, ChronoUnit> unitMap = Arrays.stream(ChronoUnit.values())
      .collect(Collectors.toMap(u -> u.toString().toLowerCase(), u -> u));


  protected long parseDiffAmount(String... args) {

    if (args.length < 2) {
      throw new InterpretException(String.format("%s filter requires a number and a string parameter", getName()));
    }

    String firstArg = args[0];
    Long diff = Longs.tryParse(firstArg);
    if (diff == null) {
      throw new InterpretException(String.format("%s filter requires a number parameter as first arg", getName()));
    }
    return diff;
  }

  protected ChronoUnit parseChronoUnit(String... args) {

    if (args.length < 2) {
      throw new InterpretException(String.format("%s filter requires a number and a string parameter", getName()));
    }

    String secondArg = args[1].toLowerCase();
    return getTemporalUnit(secondArg);
  }

  protected static ChronoUnit getTemporalUnit(String temporalUnit) {

    String lowercase = temporalUnit.toLowerCase();
    if (!unitMap.containsKey(temporalUnit)) {
      throw new InterpretException(String.format("%s is not a valid temporal unit", lowercase));
    }
    return unitMap.get(lowercase);
  }


}
