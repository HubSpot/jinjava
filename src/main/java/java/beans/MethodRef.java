//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package java.beans;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
//import sun.reflect.misc.ReflectUtil;

public final class MethodRef {
    private String signature;
    private SoftReference<Method> methodRef;
    private WeakReference<Class<?>> typeRef;

    MethodRef() {
    }

    void set(Method method) {
        if (method == null) {
            this.signature = null;
            this.methodRef = null;
            this.typeRef = null;
        } else {
            this.signature = method.toGenericString();
            this.methodRef = new SoftReference(method);
            this.typeRef = new WeakReference(method.getDeclaringClass());
        }

    }

    boolean isSet() {
        return this.methodRef != null;
    }

    Method get() {
        if (this.methodRef == null) {
            return null;
        } else {
            Method method = (Method)this.methodRef.get();
            if (method == null) {
                method = find((Class)this.typeRef.get(), this.signature);
                if (method == null) {
                    this.signature = null;
                    this.methodRef = null;
                    this.typeRef = null;
                    return null;
                }

                this.methodRef = new SoftReference(method);
            }

//            return ReflectUtil.isPackageAccessible(method.getDeclaringClass()) ? method : null;
            return method;
        }
    }

    private static Method find(Class<?> type, String signature) {
        if (type != null) {
            Method[] var2 = type.getMethods();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Method method = var2[var4];
                if (type.equals(method.getDeclaringClass()) && method.toGenericString().equals(signature)) {
                    return method;
                }
            }
        }

        return null;
    }
}
