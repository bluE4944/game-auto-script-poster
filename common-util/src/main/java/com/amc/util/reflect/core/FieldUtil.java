package com.amc.util.reflect.core;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性工具类
 */
public class FieldUtil {

    /**
     * 返回所有的属性, 不包括父类属性
     * JVM无法保证返回的顺序与声明的顺序一致
     */
    public static List<Field> getFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
    }

    /**
     * 返回所有的属性, 包括父类属性
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Set<String> fieldId = new HashSet<>();

        do {
            List<Field> fieldList = getFields(clazz).stream().filter(field -> fieldId.add(field.getName())).collect(Collectors.toList());
            result.addAll(fieldList);
            clazz = clazz.getSuperclass();
        } while (!Objects.equals(clazz, Object.class));

        return result;
    }

    /**
     * 返回所有的公共属性, 包括父类属性
     */
    public static List<Field> getPublicFields(Class<?> clazz) {
        return Arrays.stream(clazz.getFields()).collect(Collectors.toList());
    }

    /**
     * 返回指定的属性
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getAllFields(clazz).stream().filter(field -> Objects.equals(field.getName(), fieldName)).findFirst().orElse(null);
    }

    /**
     * 返回指定的属性值
     */
    public static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("无法获取指定属性值: " + field);
        }
    }

    /**
     * 设置指定的属性
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("无法设置指定属性值: " + field);
        }
    }

}
