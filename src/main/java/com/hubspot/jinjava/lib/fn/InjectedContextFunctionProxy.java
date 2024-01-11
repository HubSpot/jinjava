package com.hubspot.jinjava.lib.fn;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import com.google.common.base.Throwables;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AccessFlag;

public class InjectedContextFunctionProxy {

  private static final String GUICE_CLASS_INDICATOR = "$$EnhancerByGuice$$";

  public static ELFunctionDefinition defineProxy(
    String namespace,
    String name,
    Method m,
    Object injectedInstance
  ) {
    Class<?> injectedInstanceClass = removeGuiceWrapping(injectedInstance.getClass());
    try {
      ClassPool pool = ClassPool.getDefault();

      String ccName = String.format(
        "%s$$%s$$%s$$%s",
        injectedInstanceClass.getName(),
        InjectedContextFunctionProxy.class.getSimpleName(),
        namespace,
        name
      );
      Class<?> injectedClass = null;

      try {
        injectedClass =
          InjectedContextFunctionProxy.class.getClassLoader().loadClass(ccName);
      } catch (ClassNotFoundException e) {
        CtClass cc = pool.makeClass(ccName);
        CtClass mc = pool.get(m.getDeclaringClass().getName());

        CtField injectedField = CtField.make(
          String.format(
            "public static %s injectedField;",
            m.getDeclaringClass().getName()
          ),
          cc
        );
        cc.addField(injectedField);

        CtField injectedMethod = CtField.make(
          String.format("public static %s delegate;", Method.class.getName()),
          cc
        );
        cc.addField(injectedMethod);

        CtMethod ctMethod = mc.getDeclaredMethod(m.getName());

        CtMethod invokeMethod = CtNewMethod.make(
          Modifier.PUBLIC | Modifier.STATIC,
          ctMethod.getReturnType(),
          "invoke",
          ctMethod.getParameterTypes(),
          ctMethod.getExceptionTypes(),
          null,
          cc
        );
        invokeMethod.setBody("{ return $proceed($$); }", "injectedField", m.getName());

        for (CtClass param : ctMethod.getParameterTypes()) {
          if (param.isArray()) {
            invokeMethod.setModifiers(invokeMethod.getModifiers() | AccessFlag.VARARGS);
            break;
          }
        }

        cc.addMethod(invokeMethod);

        injectedClass = cc.toClass();
        cc.detach();
      }

      injectedClass.getField("injectedField").set(null, injectedInstance);
      injectedClass.getField("delegate").set(null, m);

      Method staticMethod = null;
      for (Method m1 : injectedClass.getMethods()) {
        if (m1.getName().equals("invoke")) {
          staticMethod = m1;
          break;
        }
      }

      return new ELFunctionDefinition(namespace, name, staticMethod);
    } catch (Throwable e) {
      ENGINE_LOG.error("Error creating injected context function", e);
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  public static Class<?> removeGuiceWrapping(Class<?> clazz) {
    if (
      clazz.getName().contains(GUICE_CLASS_INDICATOR) && clazz.getSuperclass() != null
    ) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }
}
