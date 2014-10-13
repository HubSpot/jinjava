package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FloatFilter implements Filter {

  @Override
  public String getName() {
    return "float";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    float defaultVal = 0;
    if(args.length > 0) {
      defaultVal = NumberUtils.toFloat(args[0], 0.0f);
    }
    
    if(var == null) {
      return defaultVal;
    }
    
    if(Number.class.isAssignableFrom(var.getClass())) {
      return ((Number) var).floatValue();
    }
    
    return NumberUtils.toFloat(var.toString(), defaultVal);
  }

}
