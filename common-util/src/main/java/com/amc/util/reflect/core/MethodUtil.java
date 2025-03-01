package com.amc.util.reflect.core;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 方法工具类
 */
public class MethodUtil {

    /**
     * 返回所有的方法, 不包括父类方法
     * JVM无法保证返回的顺序与声明的顺序一致
     */
    public static List<Method> getMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).collect(Collectors.toList());
    }

    /**
     * 返回所有的方法, 包括父类方法
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        Set<String> methodId = new HashSet<>();

        do {
            List<Method> methodList = getMethods(clazz).stream().filter(method -> methodId.add(getMethodSignature(method))).collect(Collectors.toList());
            result.addAll(methodList);
            clazz = clazz.getSuperclass();
        } while (!Objects.equals(clazz, Object.class));

        return result;
    }

    /**
     * 返回所有的公共方法, 包括父类方法
     */
    public static List<Method> getPublicMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods()).collect(Collectors.toList());
    }

    /**
     * 返回所有指定的方法
     */
    public static List<Method> getMethods(Class<?> clazz, String methodName) {
        return getAllMethods(clazz).stream().filter(method -> Objects.equals(method.getName(), methodName)).collect(Collectors.toList());
    }

    /**
     * 返回所有指定的方法
     */
    public static List<Method> getMethods(Class<?> clazz, String methodName, int parameterCount) {
        return getMethods(clazz, methodName).stream().filter(method -> Objects.equals(method.getParameterCount(), parameterCount)).collect(Collectors.toList());
    }

    /**
     * 返回指定的方法
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        List<Method> methods = getMethods(clazz, methodName);

        int size = methods.size();
        if (Objects.equals(size, 0)) return null;
        if (Objects.equals(size, 1)) return methods.get(0);

        List<String> parameterType = Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.toList());
        String methodId = methodName + parameterType;
        return methods.stream().filter(method -> Objects.equals(getMethodSignature(method), methodId)).findFirst().orElse(null);
    }

    /**
     * 执行指定的方法
     */
    public static Object invokeMethod(Object obj, Method method, Object... parameterValues) {
        try {
            method.setAccessible(true);
            return method.invoke(obj, parameterValues);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无法执行指定方法: " + method);
        }
    }

    /**
     * 返回方法签名
     */
    public static String getMethodSignature(Method method) {
        String methodName = method.getName();
        List<String> parameterType = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
        return methodName + parameterType;
    }

}
