package com.amc.javafx.util.spring;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 基于spring的注解解析工具
 */
public class AnnotationUtil {

    /**
     * 判断类上是否存在该注解
     */
    public static boolean hasAnnotation(String className, Class<? extends Annotation> annotationClass) {
        try {
            MetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory();
            MetadataReader metadataReader = readerFactory.getMetadataReader(className);
            AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
            return annotationMetadata.isAnnotated(annotationClass.getName());
        } catch (Exception e) {
            throw new RuntimeException("解析类注解元数据失败");
        }
    }

    /**
     * 判断类上是否存在该注解
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return Objects.nonNull(getAnnotation(clazz, annotationClass));
    }

    /**
     * 返回类上的指定注解
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        return AnnotatedElementUtils.getMergedAnnotation(clazz, annotationClass);
    }

}
