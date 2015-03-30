package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc("returns string value of object")
public class StringFilter implements Filter {

  @Override
  public String getName() {
    return "string";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return Objects.toString(var);
  }

}
