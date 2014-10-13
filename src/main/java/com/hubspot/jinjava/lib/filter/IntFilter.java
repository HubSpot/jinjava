package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

/**
 * int(value, default=0)
 *   Convert the value into an integer. If the conversion doesn’t work it will return 0. 
 *   You can override this default using the first parameter.
 */
public class IntFilter implements Filter {

  @Override
  public String getName() {
    return "int";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter,
      String... args) {
    
    int defaultVal = 0;
    if(args.length > 0) {
      defaultVal = NumberUtils.toInt(args[0], 0);
    }
    
    if(var == null) {
      return defaultVal;
    }
    
    if(Number.class.isAssignableFrom(var.getClass())) {
      return ((Number) var).intValue();
    }
    
    return NumberUtils.toInt(var.toString(), defaultVal);
  }

}
