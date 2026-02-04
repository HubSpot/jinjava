//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package java.beans;

import com.hubspot.jinjava.el.android.NameGenerator;

import java.lang.ref.Reference;
import java.lang.reflect.Method;

public class PropertyDescriptor extends FeatureDescriptor {
    private Reference<? extends Class<?>> propertyTypeRef;
    private final MethodRef readMethodRef;
    private final MethodRef writeMethodRef;
    private Reference<? extends Class<?>> propertyEditorClassRef;
    private boolean bound;
    private boolean constrained;
    private String baseName;
    private String writeMethodName;
    private String readMethodName;

    public PropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
        this(propertyName, beanClass, "is" + com.hubspot.jinjava.el.android.NameGenerator.capitalize(propertyName), "set" + com.hubspot.jinjava.el.android.NameGenerator.capitalize(propertyName));
    }

    public PropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName, String writeMethodName) throws IntrospectionException {
        this.readMethodRef = new MethodRef();
        this.writeMethodRef = new MethodRef();
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        } else if (propertyName != null && propertyName.length() != 0) {
            if (!"".equals(readMethodName) && !"".equals(writeMethodName)) {
                this.setName(propertyName);
                this.setClass0(beanClass);
                this.readMethodName = readMethodName;
                if (readMethodName != null && this.getReadMethod() == null) {
                    throw new IntrospectionException("Method not found: " + readMethodName);
                } else {
                    this.writeMethodName = writeMethodName;
                    if (writeMethodName != null && this.getWriteMethod() == null) {
                        throw new IntrospectionException("Method not found: " + writeMethodName);
                    } else {
//                        Class<?>[] args = new Class[]{PropertyChangeListener.class};
//                        this.bound = null != Introspector.findMethod(beanClass, "addPropertyChangeListener", args.length, args);
                        this.bound = false;
                    }
                }
            } else {
                throw new IntrospectionException("read or write method name should not be the empty string");
            }
        } else {
            throw new IntrospectionException("bad property name");
        }
    }

    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod) throws IntrospectionException {
        this.readMethodRef = new MethodRef();
        this.writeMethodRef = new MethodRef();
        if (propertyName != null && propertyName.length() != 0) {
            this.setName(propertyName);
            this.setReadMethod(readMethod);
            this.setWriteMethod(writeMethod);
        } else {
            throw new IntrospectionException("bad property name");
        }
    }

    public synchronized Class<?> getPropertyType() {
        Class<?> type = this.getPropertyType0();
        if (type == null) {
            try {
                type = this.findPropertyType(this.getReadMethod(), this.getWriteMethod());
                this.setPropertyType(type);
            } catch (IntrospectionException var3) {
            }
        }

        return type;
    }

    private void setPropertyType(Class<?> type) {
        this.propertyTypeRef = getWeakReference(type);
    }

    private Class<?> getPropertyType0() {
        return this.propertyTypeRef != null ? (Class)this.propertyTypeRef.get() : null;
    }

    private static Method getGetterMethod(Class<?> clazz, String fieldName) {
        String capitalizedFieldName = com.hubspot.jinjava.el.android.NameGenerator.capitalize(fieldName);
        String methodName = "get" + capitalizedFieldName;
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // 尝试 isXxx() 方法
            methodName = "is" + capitalizedFieldName;
            try {
                return clazz.getMethod(methodName);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }

    private static Method getSetterMethod(Class<?> clazz, String fieldName) {
        String capitalizedFieldName = com.hubspot.jinjava.el.android.NameGenerator.capitalize(fieldName);
        String methodName = "set" + capitalizedFieldName;
        Class<?> fieldType = null;
        try {
            fieldType = clazz.getDeclaredField(fieldName).getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
        try {
            return clazz.getMethod(methodName, fieldType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    public synchronized Method getReadMethod() {
        Method readMethod = this.readMethodRef.get();
        if (readMethod == null) {
            Class<?> cls = this.getClass0();
            if (cls == null || this.readMethodName == null && !this.readMethodRef.isSet()) {
                return null;
            }

            String nextMethodName = "get" + this.getBaseName();
            if (this.readMethodName == null) {
                Class<?> type = this.getPropertyType0();
                if (type != Boolean.TYPE && type != null) {
                    this.readMethodName = nextMethodName;
                } else {
                    this.readMethodName = "is" + this.getBaseName();
                }
            }
            readMethod = Introspector.findMethod(cls, this.readMethodName, 0);
            if (readMethod == null && !this.readMethodName.equals(nextMethodName)) {
                this.readMethodName = nextMethodName;
                readMethod = Introspector.findMethod(cls, this.readMethodName, 0);
            }

            try {
                this.setReadMethod(readMethod);
            } catch (IntrospectionException var5) {
            }
        }

        return readMethod;
    }

    public synchronized void setReadMethod(Method readMethod) throws IntrospectionException {
        this.setPropertyType(this.findPropertyType(readMethod, this.writeMethodRef.get()));
        this.setReadMethod0(readMethod);
    }

    private void setReadMethod0(Method readMethod) {
        this.readMethodRef.set(readMethod);
        if (readMethod == null) {
            this.readMethodName = null;
        } else {
            this.setClass0(readMethod.getDeclaringClass());
            this.readMethodName = readMethod.getName();
            this.setTransient((Transient)readMethod.getAnnotation(Transient.class));
        }
    }

    public synchronized Method getWriteMethod() {
        Method writeMethod = this.writeMethodRef.get();
        if (writeMethod == null) {
            Class<?> cls = this.getClass0();
            if (cls == null || this.writeMethodName == null && !this.writeMethodRef.isSet()) {
                return null;
            }

            Class<?> type = this.getPropertyType0();
            if (type == null) {
                try {
                    type = this.findPropertyType(this.getReadMethod(), (Method)null);
                    this.setPropertyType(type);
                } catch (IntrospectionException var7) {
                    return null;
                }
            }

            if (this.writeMethodName == null) {
                this.writeMethodName = "set" + this.getBaseName();
            }

            Class<?>[] args = type == null ? null : new Class[]{type};
            writeMethod = Introspector.findMethod(cls, this.writeMethodName, 1, args);
            if (writeMethod != null && !writeMethod.getReturnType().equals(Void.TYPE)) {
                writeMethod = null;
            }

            try {
                this.setWriteMethod(writeMethod);
            } catch (IntrospectionException var6) {
            }
        }

        return writeMethod;
    }

    public synchronized void setWriteMethod(Method writeMethod) throws IntrospectionException {
        this.setPropertyType(this.findPropertyType(this.getReadMethod(), writeMethod));
        this.setWriteMethod0(writeMethod);
    }

    private void setWriteMethod0(Method writeMethod) {
        this.writeMethodRef.set(writeMethod);
        if (writeMethod == null) {
            this.writeMethodName = null;
        } else {
            this.setClass0(writeMethod.getDeclaringClass());
            this.writeMethodName = writeMethod.getName();
            this.setTransient((Transient)writeMethod.getAnnotation(Transient.class));
        }
    }

    void setClass0(Class<?> clz) {
        if (this.getClass0() == null || !clz.isAssignableFrom(this.getClass0())) {
            super.setClass0(clz);
        }
    }

    public boolean isBound() {
        return this.bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public boolean isConstrained() {
        return this.constrained;
    }

    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }

    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        this.propertyEditorClassRef = getWeakReference(propertyEditorClass);
    }

    public Class<?> getPropertyEditorClass() {
        return this.propertyEditorClassRef != null ? (Class)this.propertyEditorClassRef.get() : null;
    }
//
//    public PropertyEditor createPropertyEditor(Object bean) {
//        Object editor = null;
//        Class<?> cls = this.getPropertyEditorClass();
//        if (cls != null && PropertyEditor.class.isAssignableFrom(cls) && ReflectUtil.isPackageAccessible(cls)) {
//            Constructor<?> ctor = null;
//            if (bean != null) {
//                try {
//                    ctor = cls.getConstructor(Object.class);
//                } catch (Exception var7) {
//                }
//            }
//
//            try {
//                if (ctor == null) {
//                    editor = cls.newInstance();
//                } else {
//                    editor = ctor.newInstance(bean);
//                }
//            } catch (Exception var6) {
//            }
//        }
//
//        return (PropertyEditor)editor;
//    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            if (obj != null && obj instanceof PropertyDescriptor) {
                PropertyDescriptor other = (PropertyDescriptor)obj;
                Method otherReadMethod = other.getReadMethod();
                Method otherWriteMethod = other.getWriteMethod();
                if (!this.compareMethods(this.getReadMethod(), otherReadMethod)) {
                    return false;
                }

                if (!this.compareMethods(this.getWriteMethod(), otherWriteMethod)) {
                    return false;
                }

                if (this.getPropertyType() == other.getPropertyType() && this.getPropertyEditorClass() == other.getPropertyEditorClass() && this.bound == other.isBound() && this.constrained == other.isConstrained() && this.writeMethodName == other.writeMethodName && this.readMethodName == other.readMethodName) {
                    return true;
                }
            }

            return false;
        }
    }

    boolean compareMethods(Method a, Method b) {
        if (a == null != (b == null)) {
            return false;
        } else {
            return a == null || b == null || a.equals(b);
        }
    }

    PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x, y);
        this.readMethodRef = new MethodRef();
        this.writeMethodRef = new MethodRef();
        if (y.baseName != null) {
            this.baseName = y.baseName;
        } else {
            this.baseName = x.baseName;
        }

        if (y.readMethodName != null) {
            this.readMethodName = y.readMethodName;
        } else {
            this.readMethodName = x.readMethodName;
        }

        if (y.writeMethodName != null) {
            this.writeMethodName = y.writeMethodName;
        } else {
            this.writeMethodName = x.writeMethodName;
        }

        if (y.propertyTypeRef != null) {
            this.propertyTypeRef = y.propertyTypeRef;
        } else {
            this.propertyTypeRef = x.propertyTypeRef;
        }

        Method xr = x.getReadMethod();
        Method yr = y.getReadMethod();

        try {
            if (this.isAssignable(xr, yr)) {
                this.setReadMethod(yr);
            } else {
                this.setReadMethod(xr);
            }
        } catch (IntrospectionException var10) {
        }

        if (xr != null && yr != null && xr.getDeclaringClass() == yr.getDeclaringClass() && getReturnType(this.getClass0(), xr) == Boolean.TYPE && getReturnType(this.getClass0(), yr) == Boolean.TYPE && xr.getName().indexOf("is") == 0 && yr.getName().indexOf("get") == 0) {
            try {
                this.setReadMethod(xr);
            } catch (IntrospectionException var9) {
            }
        }

        Method xw = x.getWriteMethod();
        Method yw = y.getWriteMethod();

        try {
            if (yw != null) {
                this.setWriteMethod(yw);
            } else {
                this.setWriteMethod(xw);
            }
        } catch (IntrospectionException var8) {
        }

        if (y.getPropertyEditorClass() != null) {
            this.setPropertyEditorClass(y.getPropertyEditorClass());
        } else {
            this.setPropertyEditorClass(x.getPropertyEditorClass());
        }

        this.bound = x.bound | y.bound;
        this.constrained = x.constrained | y.constrained;
    }

    PropertyDescriptor(PropertyDescriptor old) {
        super(old);
        this.readMethodRef = new MethodRef();
        this.writeMethodRef = new MethodRef();
        this.propertyTypeRef = old.propertyTypeRef;
        this.readMethodRef.set(old.readMethodRef.get());
        this.writeMethodRef.set(old.writeMethodRef.get());
        this.propertyEditorClassRef = old.propertyEditorClassRef;
        this.writeMethodName = old.writeMethodName;
        this.readMethodName = old.readMethodName;
        this.baseName = old.baseName;
        this.bound = old.bound;
        this.constrained = old.constrained;
    }

    void updateGenericsFor(Class<?> type) {
        this.setClass0(type);

        try {
            this.setPropertyType(this.findPropertyType(this.readMethodRef.get(), this.writeMethodRef.get()));
        } catch (IntrospectionException var3) {
            this.setPropertyType((Class)null);
        }

    }

    private Class<?> findPropertyType(Method readMethod, Method writeMethod) throws IntrospectionException {
        Class<?> propertyType = null;

        try {
            Class[] params;
            if (readMethod != null) {
                params = getParameterTypes(this.getClass0(), readMethod);
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: " + readMethod);
                }

                propertyType = getReturnType(this.getClass0(), readMethod);
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " + readMethod.getName() + " returns void");
                }
            }

            if (writeMethod != null) {
                params = getParameterTypes(this.getClass0(), writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: " + writeMethod);
                }

                if (propertyType != null && !params[0].isAssignableFrom(propertyType)) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }

                propertyType = params[0];
            }

            return propertyType;
        } catch (IntrospectionException var5) {
            throw var5;
        }
    }

    public int hashCode() {
        int result = 7;
        result = 37 * result + (this.getPropertyType() == null ? 0 : this.getPropertyType().hashCode());
        result = 37 * result + (this.getReadMethod() == null ? 0 : this.getReadMethod().hashCode());
        result = 37 * result + (this.getWriteMethod() == null ? 0 : this.getWriteMethod().hashCode());
        result = 37 * result + (this.getPropertyEditorClass() == null ? 0 : this.getPropertyEditorClass().hashCode());
        result = 37 * result + (this.writeMethodName == null ? 0 : this.writeMethodName.hashCode());
        result = 37 * result + (this.readMethodName == null ? 0 : this.readMethodName.hashCode());
        result = 37 * result + this.getName().hashCode();
        result = 37 * result + (!this.bound ? 0 : 1);
        result = 37 * result + (!this.constrained ? 0 : 1);
        return result;
    }

    String getBaseName() {
        if (this.baseName == null) {
            this.baseName = NameGenerator.capitalize(this.getName());
        }

        return this.baseName;
    }

    void appendTo(StringBuilder sb) {
        appendTo(sb, "bound", this.bound);
        appendTo(sb, "constrained", this.constrained);
        appendTo(sb, "propertyEditorClass", this.propertyEditorClassRef);
        appendTo(sb, "propertyType", this.propertyTypeRef);
        appendTo(sb, "readMethod", this.readMethodRef.get());
        appendTo(sb, "writeMethod", this.writeMethodRef.get());
    }

    boolean isAssignable(Method m1, Method m2) {
        if (m1 == null) {
            return true;
        } else if (m2 == null) {
            return false;
        } else if (!m1.getName().equals(m2.getName())) {
            return true;
        } else {
            Class<?> type1 = m1.getDeclaringClass();
            Class<?> type2 = m2.getDeclaringClass();
            if (!type1.isAssignableFrom(type2)) {
                return false;
            } else {
                type1 = getReturnType(this.getClass0(), m1);
                type2 = getReturnType(this.getClass0(), m2);
                if (!type1.isAssignableFrom(type2)) {
                    return false;
                } else {
                    Class<?>[] args1 = getParameterTypes(this.getClass0(), m1);
                    Class<?>[] args2 = getParameterTypes(this.getClass0(), m2);
                    if (args1.length != args2.length) {
                        return true;
                    } else {
                        for(int i = 0; i < args1.length; ++i) {
                            if (!args1[i].isAssignableFrom(args2[i])) {
                                return false;
                            }
                        }

                        return true;
                    }
                }
            }
        }
    }
}
