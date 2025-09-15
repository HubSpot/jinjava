package com.hubspot.jinjava.el.ext;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.el.ELContext;
import javax.el.MethodNotFoundException;

/**
 * {@link BeanELResolver} supporting snake case property names.
 */
public class JinjavaBeanELResolver extends BeanELResolver {

  private static final Set<String> DEFAULT_RESTRICTED_PROPERTIES = ImmutableSet
    .<String>builder()
    .add("class")
    .build();

  private static final Set<String> DEFAULT_RESTRICTED_METHODS = ImmutableSet
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

  private static final Set<String> DEFERRED_EXECUTION_RESTRICTED_METHODS = ImmutableSet
    .<String>builder()
    .add("put")
    .add("putAll")
    .add("update")
    .add("add")
    .add("insert")
    .add("pop")
    .add("append")
    .add("extend")
    .add("clear")
    .add("remove")
    .add("addAll")
    .add("removeAll")
    .add("replace")
    .add("replaceAll")
    .add("putIfAbsent")
    .add("sort")
    .add("set")
    .add("merge")
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
    if (isRestrictedClass(base)) {
      return null;
    }
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
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

    if (
      method == null ||
      DEFAULT_RESTRICTED_METHODS.contains(method.toString()) ||
      (interpreter != null &&
        interpreter.getConfig().getRestrictedMethods().contains(method.toString()))
    ) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    if (isRestrictedClass(base)) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    if (
      DEFERRED_EXECUTION_RESTRICTED_METHODS.contains(method.toString()) &&
      EagerReconstructionUtils.isDeferredExecutionMode()
    ) {
      throw new DeferredValueException(
        String.format(
          "Cannot run method '%s' in %s in deferred execution mode",
          method,
          base.getClass()
        )
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

  @Override
  protected Method findMethod(
    Object base,
    String name,
    Class<?>[] types,
    Object[] params,
    int paramCount
  ) {
    if (types != null) {
      return super.findMethod(base, name, types, params, paramCount);
    }
    Method varArgsMethod = null;

    Method[] methods = base.getClass().getMethods();
    List<Method> potentialMethods = new LinkedList<>();

    for (Method method : methods) {
      if (method.getName().equals(name)) {
        int formalParamCount = method.getParameterTypes().length;
        if (method.isVarArgs() && paramCount >= formalParamCount - 1) {
          varArgsMethod = method;
        } else if (paramCount == formalParamCount) {
          potentialMethods.add(findAccessibleMethod(method));
        }
      }
    }
    final Method finalVarArgsMethod = varArgsMethod;
    return potentialMethods
      .stream()
      .filter(method -> checkAssignableParameterTypes(params, method))
      .min(JinjavaBeanELResolver::pickMoreSpecificMethod)
      .orElseGet(() ->
        potentialMethods
          .stream()
          .findAny()
          .orElseGet(() ->
            finalVarArgsMethod == null ? null : findAccessibleMethod(finalVarArgsMethod)
          )
      );
  }

  private static boolean checkAssignableParameterTypes(Object[] params, Method method) {
    for (int i = 0; i < method.getParameterTypes().length; i++) {
      Class<?> paramType = method.getParameterTypes()[i];
      if (paramType.isPrimitive()) {
        paramType = MethodType.methodType(paramType).wrap().returnType();
      }
      if (params[i] != null && !paramType.isAssignableFrom(params[i].getClass())) {
        return false;
      }
    }
    return true;
  }

  private static int pickMoreSpecificMethod(Method methodA, Method methodB) {
    Class<?>[] typesA = methodA.getParameterTypes();
    Class<?>[] typesB = methodB.getParameterTypes();
    for (int i = 0; i < typesA.length; i++) {
      if (!typesA[i].isAssignableFrom(typesB[i])) {
        if (typesB[i].isPrimitive()) {
          return 1;
        }
        return -1;
      }
    }
    return 1;
  }

  private String validatePropertyName(Object property) {
    String propertyName = transformPropertyName(property);

    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

    if (
      DEFAULT_RESTRICTED_PROPERTIES.contains(propertyName) ||
      (interpreter != null &&
        interpreter.getConfig().getRestrictedProperties().contains(propertyName))
    ) {
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
      (o.getClass().getPackage() != null &&
        o.getClass().getPackage().getName().startsWith("java.lang.reflect")) ||
      o instanceof Class ||
      o instanceof ClassLoader ||
      o instanceof Thread ||
      o instanceof Method ||
      o instanceof Field ||
      o instanceof Constructor ||
      o instanceof JinjavaInterpreter
    );
  }
}
