package com.hubspot.jinjava.lib.filter;

import java.util.Arrays;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.VariableChain;

public class AttrFilter implements Filter {

  @Override
  public String getName() {
    return "attr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(args.length == 0) {
      throw new InterpretException(getName() + " requires an attr name to use", interpreter.getLineNumber());
    }
    
    return new VariableChain(Arrays.asList(args[0]), var).resolve();
  }

}
