package com.amc.util.asm;

import com.amc.util.asm.core.ClassMetaDataReader;
import com.amc.util.asm.core.MergeGenericHandler;
import com.amc.util.common.model.AnnotationMetaData;
import com.amc.util.common.model.ClassMetaData;
import com.amc.util.common.model.FieldMetaData;
import com.amc.util.common.model.MethodMetaData;
import com.amc.util.reflect.core.MethodUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassMetaData的解析工具
 */
public class ClassMetaDataUtil {

    public static ClassMetaData get(String className, String... genericClassNames) {
        ClassMetaData classMetaData = ClassMetaDataReader.readClass(className);
        return MergeGenericHandler.doMerge(classMetaData, genericClassNames);
    }

    public static boolean hasSuper(ClassMetaData metaData, Class<?> superClass) {
        Set<String> superNames = metaData.getSuperNames();
        return superNames.contains(superClass.getName());
    }

    public static boolean hasInterface(ClassMetaData metaData, Class<?> interfaceClass) {
        Set<String> interfaces = metaData.getInterfaces();
        return interfaces.contains(interfaceClass.getName());
    }

    public static boolean hasInnerClass(ClassMetaData metaData, Class<?> innerClass) {
        Set<String> innerClassNames = metaData.getInnerClassNames();
        return innerClassNames.contains(innerClass.getName());
    }

    public static boolean hasAnnotation(ClassMetaData metaData, Class<? extends Annotation> annotationClass) {
        return Objects.nonNull(getAnnotationValue(metaData, annotationClass));
    }

    public static Map<String, Object> getAnnotationValue(ClassMetaData metaData, Class<? extends Annotation> annotationClass) {
        List<AnnotationMetaData> annotationList = metaData.getAnnotationList();
        Optional<AnnotationMetaData> optional = annotationList.stream().filter(amd -> Objects.equals(amd.getClassName(), annotationClass.getName())).findFirst();
        if (optional.isPresent()) {
            AnnotationMetaData annotationMetaData = optional.get();
            return annotationMetaData.getValueMap();
        }
        return null;
    }

    public static Set<String> getAllFieldName(ClassMetaData metaData) {
        return metaData.getFieldList().stream().map(FieldMetaData::getName).collect(Collectors.toSet());
    }

    public static FieldMetaData findFieldByName(ClassMetaData metaData, String fieldName) {
        return metaData.getFieldList().stream().filter(meta -> Objects.equals(meta.getName(), fieldName)).findFirst().orElse(null);
    }

    public static Set<String> getAllMethodName(ClassMetaData metaData) {
        return metaData.getMethodList().stream().map(MethodMetaData::getName).collect(Collectors.toSet());
    }

    public static List<MethodMetaData> findMethodByName(ClassMetaData metaData, String methodName) {
        return metaData.getMethodList().stream().filter(meta -> Objects.equals(meta.getName(), methodName)).collect(Collectors.toList());
    }

    public static List<String> getMethodParamNames(Method method) {
        String className = method.getDeclaringClass().getName();
        ClassMetaData classMetaData = get(className);
        String methodName = method.getName();
        String methodSignature = MethodUtil.getMethodSignature(method);

        for (MethodMetaData methodMetaData : findMethodByName(classMetaData, methodName)) {
            if (Objects.equals(methodSignature, methodMetaData.getSignature())) {
                return methodMetaData.getParameterList().stream().map(FieldMetaData::getName).collect(Collectors.toList());
            }
        }
        return null;
    }

}
