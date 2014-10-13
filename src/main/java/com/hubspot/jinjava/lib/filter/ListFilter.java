package com.hubspot.jinjava.lib.filter;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ListFilter implements Filter {

  @Override
  public String getName() {
    return "list";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<?> result;
    
    if(var instanceof String) {
      result = Lists.newArrayList(((String) var).toCharArray());
    }
    
    else if(Collection.class.isAssignableFrom(var.getClass())) {
      result = Lists.newArrayList((Collection<?>) var);
    }

    else {
      result = Lists.newArrayList(var);
    }
    
    return result;
  }

}
