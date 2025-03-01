package com.amc.util.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 类的元数据信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClassMetaData extends BaseMetaData {

    /**
     * 类全路径名
     */
    private String className;

    /**
     * 类的所有父类名
     */
    private Set<String> superNames;

    /**
     * 类的所有接口名
     */
    private Set<String> interfaces;

    /**
     * 类的泛型信息
     */
    private SignatureMetaData signature;

    /**
     * 类的所有内部类名
     */
    private Set<String> innerClassNames;

    /**
     * 类的所有注解信息
     */
    private List<AnnotationMetaData> annotationList;

    /**
     * 类的所有属性信息
     */
    private List<FieldMetaData> fieldList;

    /**
     * 类的所有方法信息
     */
    private List<MethodMetaData> methodList;


    public ClassMetaData() {
        this.superNames = new LinkedHashSet<>();
        this.interfaces = new LinkedHashSet<>();
        this.innerClassNames = new LinkedHashSet<>();
        this.annotationList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
        this.methodList = new ArrayList<>();
    }

}
