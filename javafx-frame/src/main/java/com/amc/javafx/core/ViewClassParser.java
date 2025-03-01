package com.amc.javafx.core;

import com.amc.javafx.interfaces.ClassParser;
import com.amc.javafx.interfaces.FieldParser;
import com.amc.javafx.interfaces.MethodParser;
import com.amc.javafx.util.spring.AnnotationUtil;
import com.amc.util.reflect.ReflectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViewClassParser implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private PluginManager pluginManager;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        for (String beanDefinitionName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (StringUtils.hasText(beanClassName) && AnnotationUtil.hasAnnotation(beanClassName, Configuration.class)) {
                Class<?> beanClass = ReflectUtil.loadClass(beanClassName);
                for (ClassParser classParser : pluginManager.getClassParsers()) {
                    classParser.resolve(beanClass, beanDefinitionName);
                }
                for (Field field : ReflectUtil.getFields(beanClass)) {
                    for (FieldParser fieldParser : pluginManager.getFieldParsers()) {
                        fieldParser.resolve(beanClass, field, beanDefinitionName);
                    }
                }
                for (Method method : ReflectUtil.getMethods(beanClass)) {
                    for (MethodParser methodParser : pluginManager.getMethodParsers()) {
                        methodParser.resolve(beanClass, method, beanDefinitionName);
                    }
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        pluginManager = applicationContext.getBean(PluginManager.class);
    }

}
