package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IsEqualToExpTest implements ExpTest {

  @Override
  public String getName() {
    return "equalto";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if(args.length == 0) {
      throw new InterpretException(getName() + " test requires 1 argument");
    }
    
    return Objects.equals(var, args[0]);
  }

}
