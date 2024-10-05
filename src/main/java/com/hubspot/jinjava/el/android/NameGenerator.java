//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.hubspot.jinjava.el.android;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

public class NameGenerator {
    private Map<Object, String> valueToName = new IdentityHashMap();
    private Map<String, Integer> nameToCount = new HashMap();

    public NameGenerator() {
    }

    public void clear() {
        this.valueToName.clear();
        this.nameToCount.clear();
    }

    public static String unqualifiedClassName(Class type) {
        if (type.isArray()) {
            return unqualifiedClassName(type.getComponentType()) + "Array";
        } else {
            String name = type.getName();
            return name.substring(name.lastIndexOf(46) + 1);
        }
    }

    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            String var10000 = name.substring(0, 1).toUpperCase(Locale.ENGLISH);
            return var10000 + name.substring(1);
        } else {
            return name;
        }
    }

    public String instanceName(Object instance) {
        if (instance == null) {
            return "null";
        } else if (instance instanceof Class) {
            return unqualifiedClassName((Class)instance);
        } else {
            String result = (String)this.valueToName.get(instance);
            if (result != null) {
                return result;
            } else {
                Class<?> type = instance.getClass();
                String className = unqualifiedClassName(type);
                Integer size = (Integer)this.nameToCount.get(className);
                int instanceNumber = size == null ? 0 : size + 1;
                this.nameToCount.put(className, instanceNumber);
                result = className + instanceNumber;
                this.valueToName.put(instance, result);
                return result;
            }
        }
    }
}
