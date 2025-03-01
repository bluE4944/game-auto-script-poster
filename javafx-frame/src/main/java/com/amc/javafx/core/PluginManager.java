package com.amc.javafx.core;

import com.amc.javafx.interfaces.*;
import com.amc.javafx.util.spring.CommonUtil;
import com.amc.util.common.model.GenericType;
import com.amc.util.reflect.ReflectUtil;
import com.amc.util.reflect.core.GenericUtil;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Getter
public class PluginManager implements ApplicationContextAware {

    private List<ClassParser> classParsers;
    private List<FieldParser> fieldParsers;
    private List<MethodParser> methodParsers;

    private List<ViewPostProcessor> viewPostProcessors;
    private List<ViewRegistry> viewRegistries;
    private List<StopMethod> stopMethods;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Class<?> pluginManagerClass = this.getClass();
        Comparator<Object> comparator = null;
        Map<String, GenericType> fieldGenerics = GenericUtil.getFieldGenerics(pluginManagerClass);

        for (Field field : ReflectUtil.getFields(pluginManagerClass)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                GenericType genericType = fieldGenerics.get(field.getName());
                Class<?> fieldType = field.getType();
                // List类型
                if (List.class.isAssignableFrom(fieldType)) {
                    List<Object> list = fieldType.isInterface() ? new ArrayList<>() : (List) ReflectUtil.newInstance(fieldType);
                    Class<?> pluginClass = genericType.getGenericTypes().get(0).getClazz();
                    for (String beanName : applicationContext.getBeanNamesForType(pluginClass)) {
                        Object pluginBean = applicationContext.getBean(beanName, pluginClass);
                        list.add(pluginBean);
                    }
                    if (comparator == null) comparator = CommonUtil.getBeanSortRule();
                    list.sort(comparator);
                    ReflectUtil.setFieldValue(this, field, list);
                }
                // Map类型
                else if (Map.class.isAssignableFrom(fieldType)) {
                    List<GenericType> mapGeneric = genericType.getGenericTypes();
                    Class<?> kClass = mapGeneric.get(0).getClazz();
                    Class<?> vClass = mapGeneric.get(1).getClazz();
                    if (kClass != String.class) {
                        throw new RuntimeException("Map的Key必须为String类型: " + field.getName());
                    }

                    Map<String, Object> map = fieldType.isInterface() ? new HashMap<>() : (Map) ReflectUtil.newInstance(fieldType);
                    for (String beanName : applicationContext.getBeanNamesForType(vClass)) {
                        Object pluginBean = applicationContext.getBean(beanName, vClass);
                        map.put(beanName, pluginBean);
                    }
                    ReflectUtil.setFieldValue(this, field, map);
                }
                // Object类型
                else {
                    Object bean = applicationContext.getBean(fieldType);
                    ReflectUtil.setFieldValue(this, field, bean);
                }
            }
        }
    }

}
