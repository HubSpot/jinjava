package com.hubspot.jinjava.el;

import de.odysseus.el.util.SimpleContext;
import java.lang.reflect.Method;
import javax.el.ELResolver;

public class JinjavaELContext extends SimpleContext {
  private MacroFunctionMapper functionMapper;

  public JinjavaELContext() {
    super();
  }

  public JinjavaELContext(ELResolver resolver) {
    super(resolver);
  }

  @Override
  public MacroFunctionMapper getFunctionMapper() {
    if (functionMapper == null) {
      functionMapper = new MacroFunctionMapper();
    }
    return functionMapper;
  }

  @Override
  public void setFunction(String prefix, String localName, Method method) {
    getFunctionMapper().setFunction(prefix, localName, method);
  }
}
