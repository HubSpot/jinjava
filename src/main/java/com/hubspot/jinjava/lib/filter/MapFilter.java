package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;

public class MapFilter implements Filter {

  @Override
  public String getName() {
    return "map";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    
    if(args.length == 0) {
      throw new InterpretException(getName() + " filter requires name of filter or attribute to apply to given sequence");
    }
    
    String attr = args[0];
    Filter apply = interpreter.getContext().getFilter(attr);
    
    List<Object> result = new ArrayList<>();
    
    while(loop.hasNext()) {
      Object val = loop.next();
      if(apply != null) {
        val = apply.filter(val, interpreter);
      }
      else {
        val = new VariableChain(Lists.newArrayList(attr), val).resolve();
      }
      
      result.add(val);
    }
    
    return result;
  }

}
