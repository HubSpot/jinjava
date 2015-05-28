package com.hubspot.jinjava.lib.fn;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.lib.SimpleLibrary;

public class FunctionLibrary extends SimpleLibrary<ELFunctionDefinition> {

  public FunctionLibrary(boolean registerDefaults) {
    super(registerDefaults);
  }

  @Override
  protected void registerDefaults() {
    register(new ELFunctionDefinition("", "datetimeformat", Functions.class, "dateTimeFormat", Object.class, String[].class));
    register(new ELFunctionDefinition("", "truncate", Functions.class, "truncate", Object.class, Object[].class));

    register(new ELFunctionDefinition("", "super", Functions.class, "renderSuperBlock"));

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
