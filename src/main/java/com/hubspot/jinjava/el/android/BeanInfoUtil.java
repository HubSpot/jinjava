package com.hubspot.jinjava.el.android;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanInfoUtil {

    public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws IntrospectionException {
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();

        // 获取所有公共字段
//        Field[] fields = clazz.getDeclaredFields();

//        for (Field field : fields) {
//            if (!Modifier.isStatic(field.getModifiers())) {
//                String fieldName = field.getName();
//                if (field.getType() == boolean.class) {
//                    PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz,"is" + NameGenerator.capitalize(fieldName), null);
//                    propertyDescriptors.add(pd);
//                } else {
//                    PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz, "get" + NameGenerator.capitalize(fieldName), null);
//                    propertyDescriptors.add(pd);
//                }
//            }
//        }

        // 获取所有公共方法
        Method[] methods = clazz.getMethods();

        ArrayList<String> addedProperties = new ArrayList<>();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                String propertyName = getPropertyName(methodName);
                if (propertyName != null) {
                    propertyName = propertyName.toLowerCase();
                    PropertyDescriptor pd = new PropertyDescriptor(propertyName, clazz, methodName, null);
                    if (!propertyDescriptors.contains(pd)) {
                        propertyDescriptors.add(pd);
                        addedProperties.add(propertyName);
                    }
                }
            }
        }
//        new Throwable("addedProperties " + addedProperties).printStackTrace();

        return propertyDescriptors;
    }

    private static String getPropertyName(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            return methodName.substring(2);
        }
        return null;
    }
}
