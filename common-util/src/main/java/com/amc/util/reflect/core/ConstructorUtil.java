package com.amc.util.reflect.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConstructorUtil {

    /**
     * 返回所有的构造方法
     */
    public static List<Constructor<?>> getConstructors(Class<?> clazz) {
        Constructor<?>[] result = clazz.getDeclaredConstructors();
        return new ArrayList<>(Arrays.asList(result));
    }

    /**
     * 返回所有的公共构造方法
     */
    public static List<Constructor<?>> getPublicConstructors(Class<?> clazz) {
        Constructor<?>[] result = clazz.getConstructors();
        return new ArrayList<>(Arrays.asList(result));
    }

    /**
     * 返回所有指定的构造方法
     */
    public static List<Constructor<?>> getConstructors(Class<?> clazz, int parameterCount) {
        return getConstructors(clazz).stream().filter(constructor -> Objects.equals(constructor.getParameterCount(), parameterCount)).collect(Collectors.toList());
    }

    /**
     * 返回指定的构造方法
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (Exception e) {
            throw new RuntimeException("无法获取指定构造方法: " + clazz);
        }
    }

    /**
     * 执行无参构造方法并获取返回值
     */
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> constructor = getConstructor(clazz);
        return newInstance(constructor);
    }

    /**
     * 执行构造方法并获取返回值
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... parameterValues) {
        try {
            return constructor.newInstance(parameterValues);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无法执行指定构造方法: " + constructor);
        }
    }

}
