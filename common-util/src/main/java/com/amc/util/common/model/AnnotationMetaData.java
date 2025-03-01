package com.amc.util.common.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注解的元数据信息
 */
@Data
public class AnnotationMetaData {

    /**
     * 注解类全路径名
     */
    private String className;

    /**
     * 注解键值对
     * Class, Enum类型的值会转为String
     * Annotation类型的值会转为AnnotationMetaData
     * 数组类型的值会转为List
     */
    private Map<String, Object> valueMap;


    public AnnotationMetaData(String className) {
        this.className = className;
        this.valueMap = new LinkedHashMap<>();
    }

}
