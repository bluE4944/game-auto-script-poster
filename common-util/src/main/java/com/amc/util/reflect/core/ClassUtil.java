package com.amc.util.reflect.core;

import java.lang.reflect.Array;

public class ClassUtil {

    /**
     * 根据类全路径名获取类对象
     */
    public static Class<?> loadClass(String className) {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException("类加载失败: " + className);
        }
    }

    /**
     * 根据类对象获取其数组类
     */
    public static Class<?> getArrayClass(Class<?> clazz, int dimension) {
        for (int i = 0; i < dimension; i++) {
            Object array = Array.newInstance(clazz, 0);
            clazz = array.getClass();
        }
        return clazz;
    }

    /**
     * 判断对象是否匹配类型
     */
    public static boolean isInstanceof(Object object, Class<?> clazz) {
        Class<?> objClass = object instanceof Class ? (Class<?>) object : object.getClass();
        return clazz.isAssignableFrom(objClass);
    }

    /**
     * 判定是否为简单类型
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return  clazz == String.class ||
                Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz == Character.class ||
                clazz == long.class || clazz == double.class ||
                clazz == int.class || clazz == float.class ||
                clazz == short.class || clazz == char.class ||
                clazz == byte.class || clazz == boolean.class;
    }

}
