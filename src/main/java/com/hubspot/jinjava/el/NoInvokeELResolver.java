package com.hubspot.jinjava.el;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * An ELResolver that is read only and does not allow invocation of methods.
 * It is unknown whether the results of these resolver calls will be committed,
 * so disallows modification and invocation which may result in modification of values.
 */
public class NoInvokeELResolver extends ELResolver {

  private ELResolver delegate;

  public NoInvokeELResolver(ELResolver delegate) {
    this.delegate = delegate;
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
    return delegate.getCommonPropertyType(elContext, base);
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(
    ELContext elContext,
    Object base
  ) {
    return delegate.getFeatureDescriptors(elContext, base);
  }

  @Override
  public Class<?> getType(ELContext elContext, Object base, Object property) {
    return delegate.getType(elContext, base, property);
  }

  @Override
  public Object getValue(ELContext elContext, Object base, Object property) {
    return delegate.getValue(elContext, base, property);
  }

  @Override
  public boolean isReadOnly(ELContext elContext, Object base, Object property) {
    return true;
  }

  @Override
  public void setValue(ELContext elContext, Object base, Object property, Object value) {
    throw new DeferredParsingException("NoInvokeELResolver");
  }

  @Override
  public Object invoke(
    ELContext context,
    Object base,
    Object method,
    Class<?>[] paramTypes,
    Object[] params
  ) {
    throw new DeferredParsingException("NoInvokeELResolver");
  }
}
