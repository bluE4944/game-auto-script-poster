package com.amc.javafx.util.spring;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CommonUtil {

    /**
     * 生成beanName
     */
    public static String getBeanName(Class<?> clazz) {
        Component component = AnnotationUtil.getAnnotation(clazz, Component.class);
        String beanName = component.value();
        if (StringUtils.isEmpty(beanName)) {
            String shortClassName = ClassUtils.getShortName(clazz);
            beanName = Introspector.decapitalize(shortClassName);
        }
        return beanName;
    }

    /**
     * 获取非抽象方法的参数名
     */
    public static List<String> getMethodParamNames(Method method) {
        ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        return Arrays.asList(parameterNames);
    }

    /**
     * 获取bean的排序规则
     */
    public static Comparator<Object> getBeanSortRule() {
        return Comparator.comparingInt(bean -> {
            Order order = AnnotationUtil.getAnnotation(bean.getClass(), Order.class);
            return order != null ? order.value() : Integer.MAX_VALUE;
        });
    }

}
