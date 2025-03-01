package com.amc.util.reflect;

import com.amc.util.reflect.core.ClassUtil;
import com.amc.util.reflect.core.ConstructorUtil;
import com.amc.util.reflect.core.FieldUtil;
import com.amc.util.reflect.core.MethodUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 反射工具类
 */
public class ReflectUtil {

    public static Class<?> loadClass(String className) {
        return ClassUtil.loadClass(className);
    }

    public static boolean isInstanceof(Object object, Class<?> clazz) {
        return ClassUtil.isInstanceof(object, clazz);
    }

    public static <T> T newInstance(Class<T> clazz) {
        return ConstructorUtil.newInstance(clazz);
    }

    public static List<Field> getFields(Class<?> clazz) {
        return FieldUtil.getFields(clazz);
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        return FieldUtil.getAllFields(clazz);
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return FieldUtil.getField(clazz, fieldName);
    }

    public static Object getFieldValue(Object obj, Field field) {
        return FieldUtil.getFieldValue(obj, field);
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        FieldUtil.setFieldValue(obj, field, value);
    }

    public static List<Method> getMethods(Class<?> clazz) {
        return MethodUtil.getMethods(clazz);
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        return MethodUtil.getAllMethods(clazz);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return MethodUtil.getMethod(clazz, methodName, parameterTypes);
    }

    public static Object invokeMethod(Object obj, Method method, Object... parameterValues) {
        return MethodUtil.invokeMethod(obj, method, parameterValues);
    }

}
