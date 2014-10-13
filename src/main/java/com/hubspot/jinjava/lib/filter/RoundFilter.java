package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class RoundFilter implements Filter {

  @Override
  public String getName() {
    return "round";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    BigDecimal result = BigDecimal.ZERO;
    try {
      result = new BigDecimal(Objects.toString(var));
    }
    catch(NumberFormatException e) {}
    
    int precision = 0;
    if(args.length > 0) {
      precision = NumberUtils.toInt(args[0]);
    }
    
    String method = "common";
    if(args.length > 1) {
      method = args[1];
    }

    RoundingMode roundingMode;
    
    switch(method) {
    case "ceil":
      roundingMode = RoundingMode.CEILING;
      break;
    case "floor":
      roundingMode = RoundingMode.FLOOR;
      break;
    case "common":
    default:
      roundingMode = RoundingMode.HALF_UP;
    }
    
    return result.setScale(precision, roundingMode);
  }

}
