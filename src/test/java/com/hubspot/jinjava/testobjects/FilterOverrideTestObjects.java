package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

public class FilterOverrideTestObjects {

  public static class DescriptiveAddFilter implements Filter {

    @Override
    public String getName() {
      return "add";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return (
        var +
        " + " +
        args[0] +
        " = " +
        (Integer.parseInt(var.toString()) + Integer.parseInt(args[0]))
      );
    }
  }
}
