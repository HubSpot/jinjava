package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;

public class JoinFilter implements Filter {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<String> vals = new ArrayList<>();
    
    String separator = "";
    if(args.length > 0) {
      separator = args[0];
    }
    
    String attr = null;
    if(args.length > 1) {
      attr = args[1];
    }
    
    ForLoop loop = ObjectIterator.getLoop(var);
    while(loop.hasNext()) {
      Object val = loop.next();
      
      if(attr != null) {
        val = new VariableChain(Lists.newArrayList(attr), val).resolve();
      }
      
      vals.add(Objects.toString(val, ""));
    }
    
    return Joiner.on(separator).join(vals);
  }

}
