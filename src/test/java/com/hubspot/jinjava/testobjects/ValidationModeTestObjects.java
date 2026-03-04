package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

public class ValidationModeTestObjects {

  public static class ValidationFilter implements Filter {

    private int executionCount = 0;

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      executionCount++;
      return var;
    }

    public int getExecutionCount() {
      return executionCount;
    }

    @Override
    public String getName() {
      return "validation_filter";
    }
  }
}
