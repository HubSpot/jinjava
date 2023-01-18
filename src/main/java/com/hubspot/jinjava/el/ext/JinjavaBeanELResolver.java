package com.hubspot.jinjava.el.ext;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
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

  private ExpressionFactory defaultFactory;

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
    Object result;
    if (paramTypes == null) {
      result = invokeBestMatch(context, base, method, params);
    } else {
      result = super.invoke(context, base, method, paramTypes, params);
    }
    if (isRestrictedClass(result)) {
      throw new MethodNotFoundException(
        "Cannot find method '" + method + "' in " + base.getClass()
      );
    }

    return result;
  }

  protected Object invokeBestMatch(
    ELContext context,
    Object base,
    Object method,
    Object[] params
  ) {
    if (context == null) {
      throw new NullPointerException();
    } else {
      Object result = null;
      if (base != null) {
        if (params == null) {
          params = new Object[0];
        }

        String name = method.toString();
        Method target = this.findMethod(base, name, params, params.length);
        if (target == null) {
          throw new MethodNotFoundException(
            "Cannot find method " +
            name +
            " with " +
            params.length +
            " parameters in " +
            base.getClass()
          );
        }

        try {
          result =
            target.invoke(
              base,
              this.coerceParams(this.getExpressionFactory(context), target, params)
            );
        } catch (InvocationTargetException var10) {
          throw new ELException(var10.getCause());
        } catch (IllegalAccessException var11) {
          throw new ELException(var11);
        }

        context.setPropertyResolved(true);
      }

      return result;
    }
  }

  protected Method findMethod(Object base, String name, Object[] params, int paramCount) {
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
      .filter(
        method -> {
          for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (
              params[i] != null &&
              !method.getParameterTypes()[i].isAssignableFrom(params[i].getClass())
            ) {
              return false;
            }
          }
          return true;
        }
      )
      .findAny()
      .orElseGet(
        () ->
          potentialMethods
            .stream()
            .findAny()
            .orElseGet(
              () ->
                finalVarArgsMethod == null
                  ? null
                  : findAccessibleMethod(finalVarArgsMethod)
            )
      );
  }

  private static Method findAccessibleMethod(Method method) {
    Method result = findPublicAccessibleMethod(method);
    if (result == null && method != null && Modifier.isPublic(method.getModifiers())) {
      result = method;

      try {
        method.setAccessible(true);
      } catch (SecurityException var3) {
        result = null;
      }
    }

    return result;
  }

  private static Method findPublicAccessibleMethod(Method method) {
    if (method != null && Modifier.isPublic(method.getModifiers())) {
      if (
        !method.isAccessible() &&
        !Modifier.isPublic(method.getDeclaringClass().getModifiers())
      ) {
        Class[] arr$ = method.getDeclaringClass().getInterfaces();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
          Class<?> cls = arr$[i$];
          Method mth = null;

          try {
            mth =
              findPublicAccessibleMethod(
                cls.getMethod(method.getName(), method.getParameterTypes())
              );
            if (mth != null) {
              return mth;
            }
          } catch (NoSuchMethodException var8) {}
        }

        Class<?> cls = method.getDeclaringClass().getSuperclass();
        if (cls != null) {
          Method mth = null;

          try {
            mth =
              findPublicAccessibleMethod(
                cls.getMethod(method.getName(), method.getParameterTypes())
              );
            if (mth != null) {
              return mth;
            }
          } catch (NoSuchMethodException var7) {}
        }

        return null;
      } else {
        return method;
      }
    } else {
      return null;
    }
  }

  private Object[] coerceParams(
    ExpressionFactory factory,
    Method method,
    Object[] params
  ) {
    Class<?>[] types = method.getParameterTypes();
    Object[] args = new Object[types.length];
    int varargIndex;
    if (method.isVarArgs()) {
      varargIndex = types.length - 1;
      if (params.length < varargIndex) {
        throw new ELException("Bad argument count");
      }

      for (int i = 0; i < varargIndex; ++i) {
        this.coerceValue(args, i, factory, params[i], types[i]);
      }

      Class<?> varargType = types[varargIndex].getComponentType();
      int length = params.length - varargIndex;
      Object array = null;
      if (length == 1) {
        Object source = params[varargIndex];
        if (source != null && source.getClass().isArray()) {
          if (types[varargIndex].isInstance(source)) {
            array = source;
          } else {
            length = Array.getLength(source);
            array = Array.newInstance(varargType, length);

            for (int i = 0; i < length; ++i) {
              this.coerceValue(array, i, factory, Array.get(source, i), varargType);
            }
          }
        } else {
          array = Array.newInstance(varargType, 1);
          this.coerceValue(array, 0, factory, source, varargType);
        }
      } else {
        array = Array.newInstance(varargType, length);

        for (int i = 0; i < length; ++i) {
          this.coerceValue(array, i, factory, params[varargIndex + i], varargType);
        }
      }

      args[varargIndex] = array;
    } else {
      if (params.length != args.length) {
        throw new ELException("Bad argument count");
      }

      for (varargIndex = 0; varargIndex < args.length; ++varargIndex) {
        this.coerceValue(
            args,
            varargIndex,
            factory,
            params[varargIndex],
            types[varargIndex]
          );
      }
    }

    return args;
  }

  private void coerceValue(
    Object array,
    int index,
    ExpressionFactory factory,
    Object value,
    Class<?> type
  ) {
    if (value != null || type.isPrimitive()) {
      Array.set(array, index, factory.coerceToType(value, type));
    }
  }

  private ExpressionFactory getExpressionFactory(ELContext context) {
    Object obj = context.getContext(ExpressionFactory.class);
    if (obj instanceof ExpressionFactory) {
      return (ExpressionFactory) obj;
    } else {
      if (this.defaultFactory == null) {
        this.defaultFactory = ExpressionFactory.newInstance();
      }

      return this.defaultFactory;
    }
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
      o instanceof Constructor ||
      o instanceof JinjavaInterpreter
    );
  }
}
