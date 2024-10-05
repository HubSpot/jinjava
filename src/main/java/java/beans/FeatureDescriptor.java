//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package java.beans;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;

public class FeatureDescriptor {
    private static final String TRANSIENT = "transient";
    private Reference<? extends Class<?>> classRef;
    private boolean expert;
    private boolean hidden;
    private boolean preferred;
    private String shortDescription;
    private String name;
    private String displayName;
    private Hashtable<String, Object> table;

    public FeatureDescriptor() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName == null ? this.getName() : this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isExpert() {
        return this.expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isPreferred() {
        return this.preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public String getShortDescription() {
        return this.shortDescription == null ? this.getDisplayName() : this.shortDescription;
    }

    public void setShortDescription(String text) {
        this.shortDescription = text;
    }

    public void setValue(String attributeName, Object value) {
        this.getTable().put(attributeName, value);
    }

    public Object getValue(String attributeName) {
        return this.table != null ? this.table.get(attributeName) : null;
    }

    public Enumeration<String> attributeNames() {
        return this.getTable().keys();
    }

    FeatureDescriptor(FeatureDescriptor x, FeatureDescriptor y) {
        this.expert = x.expert | y.expert;
        this.hidden = x.hidden | y.hidden;
        this.preferred = x.preferred | y.preferred;
        this.name = y.name;
        this.shortDescription = x.shortDescription;
        if (y.shortDescription != null) {
            this.shortDescription = y.shortDescription;
        }

        this.displayName = x.displayName;
        if (y.displayName != null) {
            this.displayName = y.displayName;
        }

        this.classRef = x.classRef;
        if (y.classRef != null) {
            this.classRef = y.classRef;
        }

        this.addTable(x.table);
        this.addTable(y.table);
    }

    FeatureDescriptor(FeatureDescriptor old) {
        this.expert = old.expert;
        this.hidden = old.hidden;
        this.preferred = old.preferred;
        this.name = old.name;
        this.shortDescription = old.shortDescription;
        this.displayName = old.displayName;
        this.classRef = old.classRef;
        this.addTable(old.table);
    }

    private void addTable(Hashtable<String, Object> table) {
        if (table != null && !table.isEmpty()) {
            this.getTable().putAll(table);
        }

    }

    private Hashtable<String, Object> getTable() {
        if (this.table == null) {
            this.table = new Hashtable();
        }

        return this.table;
    }

    void setTransient(Transient annotation) {
        if (annotation != null && null == this.getValue("transient")) {
            this.setValue("transient", annotation.value());
        }

    }

    boolean isTransient() {
        Object value = this.getValue("transient");
        return value instanceof Boolean ? (Boolean)value : false;
    }

    void setClass0(Class<?> cls) {
        this.classRef = getWeakReference(cls);
    }

    Class<?> getClass0() {
        return this.classRef != null ? (Class)this.classRef.get() : null;
    }

    static <T> Reference<T> getSoftReference(T object) {
        return object != null ? new SoftReference(object) : null;
    }

    static <T> Reference<T> getWeakReference(T object) {
        return object != null ? new WeakReference(object) : null;
    }

    static Class<?> getReturnType(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return method.getReturnType();
//        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericReturnType()));
    }

    static Class<?>[] getParameterTypes(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
return method.getParameterTypes();/*
        return Arrays.stream(method.getGenericParameterTypes()).map(new Function<Class<?>, Class<?>>() {
            @Override
            public Class<?> apply(Class<?> aClass) {
                TypeResolver.erase()
                return null;
            }
        }).toArray(Class[]::new);
        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericParameterTypes()));*/
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append("[name=").append(this.name);
        appendTo(sb, "displayName", (Object)this.displayName);
        appendTo(sb, "shortDescription", (Object)this.shortDescription);
        appendTo(sb, "preferred", this.preferred);
        appendTo(sb, "hidden", this.hidden);
        appendTo(sb, "expert", this.expert);
        if (this.table != null && !this.table.isEmpty()) {
            sb.append("; values={");
            Iterator var2 = this.table.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry)var2.next();
                sb.append((String)entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }

            sb.setLength(sb.length() - 2);
            sb.append("}");
        }

        this.appendTo(sb);
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb) {
    }

    static void appendTo(StringBuilder sb, String name, Reference<?> reference) {
        if (reference != null) {
            appendTo(sb, name, reference.get());
        }

    }

    static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append("; ").append(name).append("=").append(value);
        }

    }

    static void appendTo(StringBuilder sb, String name, boolean value) {
        if (value) {
            sb.append("; ").append(name);
        }

    }
}
