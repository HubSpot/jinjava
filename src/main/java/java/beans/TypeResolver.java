package java.beans;

import java.lang.reflect.*;

public class TypeResolver {

    public static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return (Class) pt.getRawType();
        } else {
            Type[] bounds;
            if (type instanceof TypeVariable) {
                TypeVariable<?> tv = (TypeVariable) type;
                bounds = tv.getBounds();
                return 0 < bounds.length ? erase(bounds[0]) : Object.class;
            } else if (type instanceof WildcardType) {
                WildcardType wt = (WildcardType) type;
                bounds = wt.getUpperBounds();
                return 0 < bounds.length ? erase(bounds[0]) : Object.class;
            } else if (type instanceof GenericArrayType) {
                GenericArrayType gat = (GenericArrayType) type;
                return Array.newInstance(erase(gat.getGenericComponentType()), 0).getClass();
            } else {
                throw new IllegalArgumentException("Unknown Type kind: " + type.getClass());
            }
        }
    }
}
