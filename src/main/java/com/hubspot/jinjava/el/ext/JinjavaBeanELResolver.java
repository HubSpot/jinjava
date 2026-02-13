package com.hubspot.jinjava.el.ext;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodNotFoundException;

/**
 * {@link BeanELResolver} supporting snake case property names.
 */
public class JinjavaBeanELResolver extends BeanELResolver {

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

  private final ConcurrentHashMap<Class<?>, BeanMethods> beanMethodsCache;

  public JinjavaBeanELResolver() {
    this(true);
  }

  /**
   * Creates a new read/write {@link JinjavaBeanELResolver}.
   */
  public JinjavaBeanELResolver(boolean readOnly) {
    super(readOnly);
    this.beanMethodsCache = new ConcurrentHashMap<Class<?>, BeanMethods>();
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    return super.getType(context, base, transformPropertyName(property));
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    return getJinjavaConfig()
      .getReturnTypeValidator()
      .validateReturnType(super.getValue(context, base, transformPropertyName(property)));
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return super.isReadOnly(context, base, transformPropertyName(property));
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    super.setValue(context, base, transformPropertyName(property), value);
  }

  @Override
  public Object invoke(
    ELContext context,
    Object base,
    Object method,
    Class<?>[] paramTypes,
    Object[] params
  ) {
    if (method == null) {
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

    return getJinjavaConfig()
      .getReturnTypeValidator()
      .validateReturnType(super.invoke(context, base, method, paramTypes, params));
  }

  @Override
  protected Method findMethod(
    Object base,
    String name,
    Class<?>[] types,
    Object[] params,
    int paramCount
  ) {
    Method method;
    if (types != null) {
      method = super.findMethod(base, name, types, params, paramCount);
    } else {
      Method varArgsMethod = null;

      Method[] methods = base.getClass().getMethods();
      List<Method> potentialMethods = new LinkedList<>();

      for (Method m : methods) {
        if (m.getName().equals(name)) {
          int formalParamCount = m.getParameterTypes().length;
          if (m.isVarArgs() && paramCount >= formalParamCount - 1) {
            varArgsMethod = m;
          } else if (paramCount == formalParamCount) {
            potentialMethods.add(findAccessibleMethod(m));
          }
        }
      }
      final Method finalVarArgsMethod = varArgsMethod;
      method =
        potentialMethods
          .stream()
          .filter(m -> checkAssignableParameterTypes(params, m))
          .min(JinjavaBeanELResolver::pickMoreSpecificMethod)
          .orElseGet(() ->
            potentialMethods
              .stream()
              .findAny()
              .orElseGet(() ->
                finalVarArgsMethod == null
                  ? null
                  : findAccessibleMethod(finalVarArgsMethod)
              )
          );
    }
    return getJinjavaConfig().getMethodValidator().validateMethod(method);
  }

  @Override
  protected Method getWriteMethod(Object base, Object property) {
    return getJinjavaConfig()
      .getMethodValidator()
      .validateMethod(super.getWriteMethod(base, property));
  }

  @Override
  protected Method getReadMethod(Object base, Object property) {
    return getJinjavaConfig()
      .getMethodValidator()
      .validateMethod(super.getReadMethod(base, property));
  }

  private static JinjavaConfig getJinjavaConfig() {
    return Objects
      .requireNonNull(
        JinjavaInterpreter.getCurrent(),
        "JinjavaInterpreter.closeablePushCurrent must be used if using a JinjavaInterpreter directly"
      )
      .getConfig();
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

  protected final class BeanMethods {

    private final Map<String, List<BeanMethod>> map = new HashMap<>();

    public BeanMethods(Class<?> baseClass) {
      MethodDescriptor[] descriptors;
      try {
        descriptors = Introspector.getBeanInfo(baseClass).getMethodDescriptors();
      } catch (IntrospectionException e) {
        throw new ELException(e);
      }
      for (MethodDescriptor descriptor : descriptors) {
        map.compute(
          descriptor.getName(),
          (k, v) -> {
            if (v == null) {
              v = new LinkedList<>();
            }
            v.add(new BeanMethod(descriptor));
            return v;
          }
        );
      }
    }

    public List<BeanMethod> getBeanMethods(String methodName) {
      return map.get(methodName);
    }
  }

  protected final class BeanMethod {

    private final MethodDescriptor descriptor;

    private Method method;

    public BeanMethod(MethodDescriptor descriptor) {
      this.descriptor = descriptor;
    }

    public Method getMethod() {
      if (method == null) {
        method = findAccessibleMethod(descriptor.getMethod());
      }
      return method;
    }
  }
}
