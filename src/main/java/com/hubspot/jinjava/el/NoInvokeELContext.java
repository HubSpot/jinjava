package com.hubspot.jinjava.el;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;

public class NoInvokeELContext extends ELContext {
  private ELContext delegate;
  private NoInvokeELResolver elResolver;

  public NoInvokeELContext(ELContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public ELResolver getELResolver() {
    if (elResolver == null) {
      elResolver = new NoInvokeELResolver(delegate.getELResolver());
    }
    return elResolver;
  }

  @Override
  public FunctionMapper getFunctionMapper() {
    return delegate.getFunctionMapper();
  }

  @Override
  public VariableMapper getVariableMapper() {
    return delegate.getVariableMapper();
  }
}
