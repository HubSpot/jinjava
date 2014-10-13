package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;

public class SelectAttrFilter implements Filter {

  @Override
  public String getName() {
    return "selectattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<Object> result = new ArrayList<>();

    if(args.length == 0) {
      throw new InterpretException(getName() + " filter requires an attr to filter on", interpreter.getLineNumber());
    }
    
    String attr = args[0];

    ExpTest expTest = interpreter.getContext().getExpTest("truthy");
    if(args.length > 1) {
      expTest = interpreter.getContext().getExpTest(args[1]);
      if(expTest == null) {
        throw new InterpretException("No expression test defined with name '" + args[1] + "'", interpreter.getLineNumber());
      }
    }
    
    ForLoop loop = ObjectIterator.getLoop(var);
    while(loop.hasNext()) {
      Object val = loop.next();
      Object attrVal = new VariableChain(Lists.newArrayList(attr), val).resolve();
      
      if(expTest.evaluate(attrVal, interpreter)) {
        result.add(val);
      }
    }
    
    return result;
  }

}
