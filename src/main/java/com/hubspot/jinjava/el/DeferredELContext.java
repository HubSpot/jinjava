package com.hubspot.jinjava.el;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public class DeferredELContext extends ELContext {
  public static final DeferredELContext INSTANCE = new DeferredELContext();

  private DeferredELContext() {}

  @Override
  public ELResolver getELResolver() {
    throw new DeferredParsingException("DeferredELContext");
  }

  @Override
  public FunctionMapper getFunctionMapper() {
    throw new DeferredParsingException("DeferredELContext");
  }

  @Override
  public VariableMapper getVariableMapper() {
    throw new DeferredParsingException("DeferredELContext");
  }
}
