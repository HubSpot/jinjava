package com.hubspot.jinjava.el.ext;

import javax.el.ELContext;
import javax.el.ListELResolver;

public class JinjavaListELResolver extends ListELResolver {

  public JinjavaListELResolver(boolean readOnly) {
    super(readOnly);
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    try {
      return super.getType(context, base, property);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    try {
      return super.isReadOnly(context, base, property);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    try {
      return super.getValue(context, base, property);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    try {
      super.setValue(context, base, property, value);
    } catch (IllegalArgumentException e) {
      /* */ }
  }

}
