package com.hubspot.jinjava.lib.fn;

import com.google.common.base.Throwables;
import com.hubspot.jinjava.lib.Importable;
import java.lang.reflect.Method;

public class ELFunctionDefinition implements Importable {

  private String namespace;
  private String localName;
  private Method method;

  public ELFunctionDefinition(
    String namespace,
    String localName,
    Class<?> methodClass,
    String methodName,
    Class<?>... parameterTypes
  ) {
    this.namespace = namespace;
    this.localName = localName;
    this.method = resolveMethod(methodClass, methodName, parameterTypes);
  }

  private static Method resolveMethod(
    Class<?> methodClass,
    String methodName,
    Class<?>... parameterTypes
  ) {
    try {
      Method m = methodClass.getDeclaredMethod(methodName, parameterTypes);
      m.setAccessible(true);
      return m;
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  public ELFunctionDefinition(String namespace, String localName, Method method) {
    this.namespace = namespace;
    this.localName = localName;
    this.method = method;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getLocalName() {
    return localName;
  }

  @Override
  public String getName() {
    return namespace + ":" + localName;
  }

  public Method getMethod() {
    return method;
  }
}
