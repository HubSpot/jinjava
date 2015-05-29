package com.hubspot.jinjava.lib.exptest;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("Return true if the given string is all lowercased")
public class IsLowerExpTest implements ExpTest {

  @Override
  public String getName() {
    return "lower";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if(var == null || !(var instanceof String)) {
      return false;
    }

    return StringUtils.isAllLowerCase((String) var);
  }

}
