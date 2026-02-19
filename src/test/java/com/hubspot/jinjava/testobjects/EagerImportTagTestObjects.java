package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

public class EagerImportTagTestObjects {

  public static class PrintPathFilter implements Filter {

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return interpreter.getContext().getCurrentPathStack().peek().orElse("/");
    }

    @Override
    public String getName() {
      return "print_path";
    }
  }
}
