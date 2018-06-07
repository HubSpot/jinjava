package com.hubspot.jinjava.lib.fn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

/**
 * Function definition parsed from a jinjava template, stored in global macros registry in interpreter context.
 *
 * @author jstehler
 *
 */
public class MacroFunction extends AbstractCallableMethod {

  private final List<Node> content;

  private final boolean catchKwargs;
  private final boolean catchVarargs;
  private final boolean caller;

  private final Context localContextScope;

  public MacroFunction(List<Node> content,
                       String name,
                       LinkedHashMap<String, Object> argNamesWithDefaults,
                       boolean catchKwargs,
                       boolean catchVarargs,
                       boolean caller,
                       Context localContextScope) {
    super(name, argNamesWithDefaults);
    this.content = content;
    this.catchKwargs = catchKwargs;
    this.catchVarargs = catchVarargs;
    this.caller = caller;
    this.localContextScope = localContextScope;
  }

  @Override
  public Object doEvaluate(Map<String, Object> argMap, Map<String, Object> kwargMap, List<Object> varArgs) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      for (Map.Entry<String, Object> scopeEntry : localContextScope.getScope().entrySet()) {
        if (scopeEntry.getValue() instanceof MacroFunction) {
          interpreter.getContext().addGlobalMacro((MacroFunction) scopeEntry.getValue());
        } else {
          interpreter.getContext().put(scopeEntry.getKey(), scopeEntry.getValue());
        }
      }

      // named parameters
      for (Map.Entry<String, Object> argEntry : argMap.entrySet()) {
        interpreter.getContext().put(argEntry.getKey(), argEntry.getValue());
      }
      // parameter map
      interpreter.getContext().put("kwargs", kwargMap);
      // varargs list
      interpreter.getContext().put("varargs", varArgs);

      LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(interpreter.getConfig().getMaxOutputSize());

      for (Node node : content) {
        result.append(node.render(interpreter));
      }

      return result.toString();
    }
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
