package com.hubspot.jinjava.util;

import com.hubspot.jinjava.interpret.InterpretException;

public class JinjavaPropertyNotResolvedException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final Object base;
  private final String property;
  
  public JinjavaPropertyNotResolvedException(Object base, String property) {
    super("Unable to resolve property: '" + property + "' in " + base);
    this.base = base;
    this.property = property;
  }

  public Object getBase() {
    return base;
  }
  
  public String getProperty() {
    return property;
  }
  
}
