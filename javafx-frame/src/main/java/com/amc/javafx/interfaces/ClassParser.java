package com.amc.javafx.interfaces;

/**
 * 类型解析器
 */
public interface ClassParser {

    void resolve(Class<?> clazz, String beanName);

}
