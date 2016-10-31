package com.hubspot.jinjava.el;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.FunctionMapper;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;

public class MacroFunctionMapper extends FunctionMapper {

  private Map<String, Method> map = Collections.emptyMap();

  private static String buildFunctionName(String prefix, String name) {
    return prefix + ":" + name;
  }

  @Override
  public Method resolveFunction(String prefix, String localName) {
    MacroFunction macroFunction = JinjavaInterpreter.getCurrent().getContext().getGlobalMacro(localName);

    if (macroFunction != null) {
      return AbstractCallableMethod.EVAL_METHOD;
    }

    return map.get(buildFunctionName(prefix, localName));
  }

  public void setFunction(String prefix, String localName, Method method) {
    if (map.isEmpty()) {
      map = new HashMap<String, Method>();
    }
    map.put(buildFunctionName(prefix, localName), method);
  }

}
