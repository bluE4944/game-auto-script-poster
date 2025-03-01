package com.amc.javafx.interfaces;

import java.lang.reflect.Field;

/**
 * 属性解析器
 */
public interface FieldParser {

    void resolve(Class<?> clazz, Field field, String beanName);

}
