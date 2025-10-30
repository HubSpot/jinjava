package java.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;

public class Introspector {
    public static String decapitalize(String name) {
        if (name != null && name.length() != 0) {
            if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
                return name;
            } else {
                char[] chars = name.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        } else {
            return name;
        }
    }

    static Method findMethod(Class<?> cls, String methodName, int argCount) {
        return findMethod(cls, methodName, argCount, (Class[])null);
    }

    static Method findMethod(Class<?> cls, String methodName, int argCount, Class<?>[] args) {
        return methodName == null ? null : internalFindMethod(cls, methodName, argCount, args);
    }

    private static Method internalFindMethod(Class<?> start, String methodName, int argCount, Class<?>[] args) {
        Class<?> cl = start;
        Method method;
        label62:
        while(cl != null) {
            Iterator<Method> var5 = Arrays.stream(cl.getMethods()).iterator();

            boolean different;
            do {
                Type[] params;
                do {
                    do {
                        if (!var5.hasNext()) {
                            cl = cl.getSuperclass();
                            continue label62;
                        }

                        method = (Method)var5.next();
                    } while(!method.getName().equals(methodName));

                    params = method.getGenericParameterTypes();
                } while(params.length != argCount);

                if (args == null) {
                    return method;
                }

                different = false;
                if (argCount <= 0) {
                    return method;
                }

//                for(int j = 0; j < argCount; ++j) {
//                    if (TypeResolver.erase(TypeResolver.resolveInClass(start, params[j])) != args[j]) {
//                        different = true;
//                    }
//                }
            } while(different);

            return method;
        }

        Class<?>[] ifcs = start.getInterfaces();

        for(int i = 0; i < ifcs.length; ++i) {
            method = internalFindMethod(ifcs[i], methodName, argCount, (Class[])null);
            if (method != null) {
                return method;
            }
        }

        return null;
    }
}
