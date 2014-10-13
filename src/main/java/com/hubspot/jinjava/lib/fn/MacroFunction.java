package com.hubspot.jinjava.lib.fn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.NodeList;

/**
 * Function definition parsed from a jinjava template, stored in global macros registry in interpreter context.
 * 
 * @author jstehler
 *
 */
public class MacroFunction extends AbstractCallableMethod {
  
  private NodeList content;
  
  private boolean catchKwargs;
  private boolean catchVarargs;
  private boolean caller;
  
  public MacroFunction(NodeList content, String name, LinkedHashMap<String, Object> argNamesWithDefaults,
      boolean catchKwargs, boolean catchVarargs, boolean caller) {
    super(name, argNamesWithDefaults);
    this.content = content;
    this.catchKwargs = catchKwargs;
    this.catchVarargs = catchVarargs;
    this.caller = caller;
  }

  @Override
  public Object doEvaluate(Map<String, Object> argMap, Map<String, Object> kwargMap, List<Object> varArgs) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    
    // named parameters
    for(Map.Entry<String, Object> argEntry : argMap.entrySet()) {
      interpreter.getContext().put(argEntry.getKey(), argEntry.getValue());
    }
    // parameter map
    interpreter.getContext().put("kwargs", argMap);
    // varargs list
    interpreter.getContext().put("varargs", varArgs);
    
    StringBuilder result = new StringBuilder();
    
    for(Node node : content) {
      result.append(node.render(interpreter));
    }
    
    return result.toString();
  }
  
  public boolean isCatchKwargs() {
    return catchKwargs;
  }

  public boolean isCatchVarargs() {
    return catchVarargs;
  }
  
  public boolean isCaller() {
    return caller;
  }
  
}
