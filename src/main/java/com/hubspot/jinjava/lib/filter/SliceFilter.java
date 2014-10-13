package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Iterators;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

public class SliceFilter implements Filter {

  @Override
  public String getName() {
    return "slice";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    
    if(args.length == 0) {
      throw new InterpretException(getName() + " requires number of slices argument", interpreter.getLineNumber());
    }
    
    int slices = NumberUtils.toInt(args[0], 3);
    return Iterators.paddedPartition(loop, slices);
  }

}
