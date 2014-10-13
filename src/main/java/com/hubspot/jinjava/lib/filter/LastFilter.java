package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class LastFilter implements Filter {

  @Override
  public String getName() {
    return "last";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    Object last = null;
    
    while(loop.hasNext()) {
      last = loop.next();
    }
    
    return last;
  }

}
