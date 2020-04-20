package com.hubspot.jinjava.el.ext;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.MethodNotFoundException;

/**
 * {@link BeanELResolver} supporting snake case property names.
 */
public class JinjavaBeanELResolver extends BeanELResolver {
  private static final Set<String> RESTRICTED_PROPERTIES = ImmutableSet
    .<String>builder()
    .add("class")
    .build();

  private static final Set<String> RESTRICTED_METHODS = ImmutableSet
    .<String>builder()
    .add("class")
    .add("clone")
    .add("hashCode")
    .add("getClass")
    .add("getDeclaringClass")
    .add("forName")
    .add("notify")
    .add("notifyAll")
    .add("wait")
    .build();

  /**
   * Creates a new read/write {@link JinjavaBeanELResolver}.
   */
  public JinjavaBeanELResolver() {}

  /**
   * Creates a new {@link JinjavaBeanELResolver} whose read-only status is determined by the given parameter.
   */
  public JinjavaBeanELResolver(boolean readOnly) {
    super(readOnly);
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    return super.getType(context, base, validatePropertyName(property));
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    Object result = super.getValue(context, base, validatePropertyName(property));
    return result instanceof Class ? null : result;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return super.isReadOnly(context, base, validatePropertyName(property));
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    super.setValue(context, base, validatePropertyName(property), value);
  }

  @Override
  public Object invoke(
    ELContext context,
    Object base,
    Object method,
    Class<?>[] paramTypes,
    Object[] params
  ) {
    if (method == null || RESTRICTED_METHODS.contains(method.toString())) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    if (isRestrictedClass(base)) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    Object result = super.invoke(context, base, method, paramTypes, params);

    if (isRestrictedClass(result)) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    return result;
  }

  private String validatePropertyName(Object property) {
    String propertyName = transformPropertyName(property);

    if (RESTRICTED_PROPERTIES.contains(propertyName)) {
      return null;
    }

    return propertyName;
  }

  /**
   * Transform snake case to property name.
   */
  private String transformPropertyName(Object property) {
    if (property == null) {
      return null;
    }

    String propertyStr = property.toString();
    if (propertyStr.indexOf('_') == -1) {
      return propertyStr;
    }
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, propertyStr);
  }

  protected boolean isRestrictedClass(Object o) {
    if (o == null) {
      return false;
    }

    return (
      (
        o.getClass().getPackage() != null &&
        o.getClass().getPackage().getName().startsWith("java.lang.reflect")
      ) ||
      o instanceof Class ||
      o instanceof ClassLoader ||
      o instanceof Thread ||
      o instanceof Method ||
      o instanceof Field ||
      o instanceof Constructor
    );
  }
}
