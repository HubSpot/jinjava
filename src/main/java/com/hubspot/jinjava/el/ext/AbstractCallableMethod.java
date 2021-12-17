package com.hubspot.jinjava.el.ext;

import com.google.common.base.Throwables;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a function which will be called in the context of an interpreter instance. Supports named params with default values, as well as var args.
 *
 * @author jstehler
 *
 */
public abstract class AbstractCallableMethod {
  public static final Method EVAL_METHOD;

  static {
    try {
      EVAL_METHOD = AbstractCallableMethod.class.getMethod("evaluate", Object[].class);
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  protected final String name;
  protected final LinkedHashMap<String, Object> argNamesWithDefaults;

  public AbstractCallableMethod(
    String name,
    LinkedHashMap<String, Object> argNamesWithDefaults
  ) {
    this.name = name;
    this.argNamesWithDefaults = argNamesWithDefaults;
  }

  public Object evaluate(Object... args) {
    Map<String, Object> argMap = new LinkedHashMap<>(argNamesWithDefaults);
    Map<String, Object> kwargMap = new LinkedHashMap<>();
    List<Object> varArgs = new ArrayList<>();

    int argPos = 0;
    for (Map.Entry<String, Object> argEntry : argMap.entrySet()) {
      if (argPos < args.length) {
        Object arg = args[argPos++];
        // once we hit the first named parameter, the rest must be named parameters...
        if (arg instanceof NamedParameter) {
          argPos--;
          break;
        }
        argEntry.setValue(arg);
      } else {
        break;
      }
    }

    // consumeth thyne named params
    for (int i = argPos; i < args.length; i++) {
      Object arg = args[i];
      if (arg instanceof NamedParameter) {
        NamedParameter param = (NamedParameter) arg;
        if (argMap.containsKey(param.getName())) {
          argMap.put(param.getName(), param.getValue());
        } else {
          kwargMap.put(param.getName(), param.getValue());
        }
      } else {
        varArgs.add(arg);
      }
    }

    return doEvaluate(argMap, kwargMap, varArgs);
  }

  public abstract Object doEvaluate(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs
  );

  public String getName() {
    return name;
  }

  public List<String> getArguments() {
    return new ArrayList<>(argNamesWithDefaults.keySet());
  }

  public Map<String, Object> getDefaults() {
    return argNamesWithDefaults;
  }
}
