package com.hubspot.jinjava.lib.fn;

import java.util.Set;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.el.ext.NamedParameter;
import com.hubspot.jinjava.lib.SimpleLibrary;

public class FunctionLibrary extends SimpleLibrary<ELFunctionDefinition> {

  public FunctionLibrary(boolean registerDefaults, Set<String> disabled) {
    super(registerDefaults, disabled);
  }

  @Override
  protected void registerDefaults() {
    register(new ELFunctionDefinition("", "datetimeformat", Functions.class, "dateTimeFormat", Object.class, String[].class));
    register(new ELFunctionDefinition("", "unixtimestamp", Functions.class, "unixtimestamp", Object.class));
    register(new ELFunctionDefinition("", "truncate", Functions.class, "truncate", Object.class, Object[].class));
    register(new ELFunctionDefinition("", "range", Functions.class, "range", Object.class, Object[].class));
    register(new ELFunctionDefinition("", "type", TypeFunction.class, "type", Object.class));
    register(new ELFunctionDefinition("", "today", Functions.class, "today", String[].class));
    register(new ELFunctionDefinition("", "strtotime", Functions.class, Functions.STRING_TO_TIME_FUNCTION, String.class, String.class));

    register(new ELFunctionDefinition("", "super", Functions.class, "renderSuperBlock"));
    register(new ELFunctionDefinition("", "namespace", Functions.class, "createNamespace", NamedParameter[].class));

    register(new ELFunctionDefinition("fn", "list", Lists.class, "newArrayList", Object[].class));
    register(new ELFunctionDefinition("fn", "immutable_list", Functions.class, "immutableListOf", Object[].class));
  }

  public void addFunction(ELFunctionDefinition fn) {
    register(fn);
  }

  public ELFunctionDefinition getFunction(String name) {
    return fetch(name);
  }

}
