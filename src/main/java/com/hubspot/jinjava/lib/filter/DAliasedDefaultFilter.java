package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(value = "", aliasOf = "default")
public class DAliasedDefaultFilter extends DefaultFilter {

  @Override
  public String getName() {
    return "d";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return null;
  }
}
