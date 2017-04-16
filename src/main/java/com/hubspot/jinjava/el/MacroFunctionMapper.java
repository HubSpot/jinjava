package com.hubspot.jinjava.el;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.FunctionMapper;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;

public class MacroFunctionMapper extends FunctionMapper {

  private Map<String, Method> map = Collections.emptyMap();

  private static String buildFunctionName(String prefix, String name) {
    return prefix + ":" + name;
  }

  @Override
  public Method resolveFunction(String prefix, String localName) {
    final Context context = JinjavaInterpreter.getCurrent().getContext();
    MacroFunction macroFunction = context.getGlobalMacro(localName);

    if (macroFunction != null) {
      return AbstractCallableMethod.EVAL_METHOD;
    }

    final String functionName = buildFunctionName(prefix, localName);

    if (context.isFunctionDisabled(functionName)) {
      throw new DisabledException(functionName);
    }

    if (map.containsKey(functionName)) {
      context.addResolvedFunction(functionName);
    }

    return map.get(functionName);
  }

  public void setFunction(String prefix, String localName, Method method) {
    if (map.isEmpty()) {
      map = new HashMap<>();
    }
    map.put(buildFunctionName(prefix, localName), method);
  }

}
