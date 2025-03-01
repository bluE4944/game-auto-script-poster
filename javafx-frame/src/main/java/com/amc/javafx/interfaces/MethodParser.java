package com.amc.javafx.interfaces;

import java.lang.reflect.Method;

/**
 * 方法解析器
 */
public interface MethodParser {

    void resolve(Class<?> clazz, Method method, String beanName);

}
