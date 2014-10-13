package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ForceEscapeFilter implements Filter {

  @Override
  public String getName() {
    return "forceescape";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return StringEscapeUtils.escapeHtml4(Objects.toString(var, ""));
  }

}
