package com.hubspot.jinjava.el;

import com.hubspot.jinjava.el.ext.AllowlistReturnTypeValidator;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

class ReturnTypeValidatingJinjavaInterpreterResolver extends ELResolver {

  private final AllowlistReturnTypeValidator returnTypeValidator;
  private final JinjavaInterpreterResolver delegate;

  ReturnTypeValidatingJinjavaInterpreterResolver(
    AllowlistReturnTypeValidator returnTypeValidator,
    JinjavaInterpreterResolver delegate
  ) {
    this.returnTypeValidator = returnTypeValidator;
    this.delegate = delegate;
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return delegate.getCommonPropertyType(context, base);
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(
    ELContext context,
    Object base
  ) {
    return delegate.getFeatureDescriptors(context, base);
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    return delegate.getType(context, base, property);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    return returnTypeValidator.validateReturnType(
      delegate.getValue(context, base, property)
    );
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return delegate.isReadOnly(context, base, property);
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    delegate.setValue(context, base, property, value);
  }

  @Override
  public Object invoke(
    ELContext context,
    Object base,
    Object method,
    Class<?>[] paramTypes,
    Object[] params
  ) {
    return returnTypeValidator.validateReturnType(
      delegate.invoke(context, base, method, paramTypes, params)
    );
  }
}
