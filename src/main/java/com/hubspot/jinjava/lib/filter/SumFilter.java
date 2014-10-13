package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;

public class SumFilter implements Filter {

  @Override
  public String getName() {
    return "sum";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    
    BigDecimal sum = BigDecimal.ZERO;
    String attr = null;
    
    if(args.length > 0) {
      attr = args[0];
    }
    if(args.length > 1) {
      try {
        sum = sum.add(new BigDecimal(args[1]));
      }
      catch(NumberFormatException e) {}
    }
    
    while(loop.hasNext()) {
      Object val = loop.next();
      if(val == null) {
        continue;
      }
      
      BigDecimal addend = BigDecimal.ZERO;
      
      if(attr != null) {
        val = new VariableChain(Arrays.asList(attr), val).resolve();
      }
      
      try {
        if(Number.class.isAssignableFrom(val.getClass())) {
          addend = new BigDecimal(((Number) val).doubleValue());
        }
        else {
          addend = new BigDecimal(Objects.toString(val, "0"));
        }
      }
      catch(NumberFormatException e) {}
      
      sum = sum.add(addend);
    }
    
    return sum;
  }

}
