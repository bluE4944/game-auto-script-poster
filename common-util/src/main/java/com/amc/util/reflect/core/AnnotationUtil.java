package com.amc.util.reflect.core;

import com.amc.util.common.annotations.AliasFor;
import com.amc.util.common.constants.SimpleConstant;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationUtil {

    /**
     * 判断类上是否存在该注解
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(clazz.getAnnotations(), annotationClass);
    }

    /**
     * 判断属性上是否存在该注解
     */
    public static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(field.getAnnotations(), annotationClass);
    }

    /**
     * 判断方法上是否存在该注解
     */
    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(method.getAnnotations(), annotationClass);
    }

    /**
     * 获取类上的指定注解
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        Object annotation = getAnnotation(clazz.getAnnotations(), annotationClass);
        return Objects.nonNull(annotation) ? (T) annotation : null;
    }

    /**
     * 获取属性上的指定注解
     */
    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        Object annotation = getAnnotation(field.getAnnotations(), annotationClass);
        return Objects.nonNull(annotation) ? (T) annotation : null;
    }

    /**
     * 获取方法上的指定注解
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        Object annotation = getAnnotation(method.getAnnotations(), annotationClass);
        return Objects.nonNull(annotation) ? (T) annotation : null;
    }

    private static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationClass) {
        List<Annotation> annotationList = Arrays.stream(annotations).collect(Collectors.toList());
        Set<String> annotationNames = annotationList.stream().map(Annotation::annotationType).map(Class::getName).collect(Collectors.toSet());
        for (int i = 0; i < annotationList.size(); i++) {
            for (Annotation annotation : annotationList.get(i).annotationType().getAnnotations()) {
                String annotationName = annotation.annotationType().getName();
                if (!annotationNames.contains(annotationName)) {
                    annotationList.add(annotation);
                    annotationNames.add(annotationName);
                }
            }
        }
        return annotationNames.contains(annotationClass.getName());
    }

    private static Object getAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationClass) {
        List<Annotation> annotationList = Arrays.stream(annotations).collect(Collectors.toList());
        Set<String> annotationNames = annotationList.stream().map(Annotation::annotationType).map(Class::getName).collect(Collectors.toSet());
        Map<Annotation, Annotation> parentMap = new HashMap<>();

        for (int i = 0; i < annotationList.size(); i++) {
            Annotation parentAnnotation = annotationList.get(i);
            for (Annotation annotation : parentAnnotation.annotationType().getAnnotations()) {
                String annotationName = annotation.annotationType().getName();
                if (!annotationNames.contains(annotationName)) {
                    annotationList.add(annotation);
                    annotationNames.add(annotationName);
                    parentMap.put(annotation, parentAnnotation);
                }
            }
        }

        Optional<Annotation> targetAnnotation = annotationList.stream().filter(annotation -> Objects.equals(annotation.annotationType(), annotationClass)).findFirst();
        if (targetAnnotation.isPresent()) {
            Annotation target = targetAnnotation.get();
            Annotation parentTarget = target;

            List<Annotation> targetAnnotations = new ArrayList<>();
            while (Objects.nonNull(parentTarget = parentMap.get(parentTarget))) {
                targetAnnotations.add(0, parentTarget);
            }
            targetAnnotations.add(target);

            return mergeAnnotation(target, targetAnnotations);
        }
        return null;
    }

    private static Object mergeAnnotation(Annotation target, List<Annotation> annotations) {
        Map<String, String> aliasMap = new HashMap<>();
        String PACKAGE_SEPARATOR = ".";

        for (Annotation an : annotations) {
            Class<? extends Annotation> anClass = an.annotationType();
            for (Method method : anClass.getMethods()) {
                AliasFor aliasFor = method.getAnnotation(AliasFor.class);
                if (Objects.nonNull(aliasFor)) {
                    Class<?> aliasForAnnotation = aliasFor.annotation();
                    if (Objects.equals(aliasForAnnotation, Annotation.class)) {
                        aliasForAnnotation = anClass;
                    }
                    String methodName = aliasFor.value();
                    if (Objects.equals(methodName, SimpleConstant.STRING)) {
                        methodName = method.getName();
                    }

                    String key = aliasForAnnotation.getName() + PACKAGE_SEPARATOR + methodName;
                    String val = anClass.getName() + PACKAGE_SEPARATOR + method.getName();
                    aliasMap.put(key, val);
                }
            }
        }

        if (!aliasMap.isEmpty()) {
            aliasMap.forEach((key, val) -> {
                String newVal = val;
                boolean needPut = false;
                while (aliasMap.containsKey(newVal)) {
                    newVal = aliasMap.get(newVal);
                    needPut = true;
                    if (Objects.equals(newVal, key) || Objects.equals(newVal, val)) {
                        break;
                    }
                }
                if (needPut) {
                    aliasMap.put(key, newVal);
                }
            });

            Map<String, Object> valMap = new HashMap<>();
            String annotationName = target.annotationType().getName();
            aliasMap.forEach((key, val) -> {
                String clazzName = key.substring(0, key.lastIndexOf(PACKAGE_SEPARATOR));
                if (Objects.equals(clazzName, annotationName)) {
                    String fieldName = key.substring(key.lastIndexOf(PACKAGE_SEPARATOR) + 1);

                    String anName = val.substring(0, val.lastIndexOf(PACKAGE_SEPARATOR));
                    String methodName = val.substring(val.lastIndexOf(PACKAGE_SEPARATOR) + 1);

                    annotations.stream()
                            .filter(an -> Objects.equals(an.annotationType().getName(), anName))
                            .findFirst()
                            .ifPresent(an -> {
                                Method method = MethodUtil.getMethod(an.getClass(), methodName);
                                Object fieldVal = MethodUtil.invokeMethod(an, method);
                                valMap.put(fieldName, fieldVal);
                            });
                }
            });

            if (!valMap.isEmpty()) {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(target);
                Field memberValues = FieldUtil.getField(invocationHandler.getClass(), "memberValues");
                Map<String, Object> fieldValue = (Map) FieldUtil.getFieldValue(invocationHandler, memberValues);
                fieldValue.putAll(valMap);
            }
        }

        return target;
    }

}