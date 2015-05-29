package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc("randomly shuffle a given list, returning a new list with all of the items of the original list in a random order")
public class ShuffleFilter implements Filter {

  @Override
  public String getName() {
    return "shuffle";
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(var instanceof Collection) {
      List<?> list = new ArrayList<Object>((Collection<Object>) var);
      Collections.shuffle(list);
      return list;
    }

    return var;
  }

}
