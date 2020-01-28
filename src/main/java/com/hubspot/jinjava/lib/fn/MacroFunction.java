package com.hubspot.jinjava.lib.fn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

  private final boolean caller;

  private final Context localContextScope;

  private final int definitionLineNumber;
  private final int definitionStartPosition;

  public MacroFunction(List<Node> content,
                       String name,
                       LinkedHashMap<String, Object> argNamesWithDefaults,
                       boolean caller,
                       Context localContextScope,
                       int lineNumber,
                       int startPosition) {
    super(name, argNamesWithDefaults);
    this.content = content;
    this.caller = caller;
    this.localContextScope = localContextScope;
    this.definitionLineNumber = lineNumber;
    this.definitionStartPosition = startPosition;
  }

  @Override
  public Object doEvaluate(Map<String, Object> argMap, Map<String, Object> kwargMap, List<Object> varArgs) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    Optional<String> importFile = Optional.ofNullable((String) localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY));

    // pushWithoutCycleCheck() is used to here so that macros calling macros from the same file will not throw a TagCycleException
    importFile.ifPresent(path -> interpreter.getContext().getCurrentPathStack().pushWithoutCycleCheck(path, interpreter.getLineNumber(), interpreter.getPosition()));

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      interpreter.setLineNumber(definitionLineNumber);
      interpreter.setPosition(definitionStartPosition);

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
    } finally {
      importFile.ifPresent(path -> interpreter.getContext().getCurrentPathStack().pop());
    }
  }

  public boolean isCaller() {
    return caller;
  }
}
