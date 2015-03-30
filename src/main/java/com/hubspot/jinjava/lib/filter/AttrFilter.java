package com.hubspot.jinjava.lib.filter;

import java.util.Arrays;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.VariableChain;

@JinjavaDoc(
    value="Get an attribute of an object. foo|attr(\"bar\") works like foo.bar just that always an attribute is returned and items are not looked up.",
    params={
        @JinjavaParam("obj"),
        @JinjavaParam(value="name", desc="attribute name")
    }
)
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
