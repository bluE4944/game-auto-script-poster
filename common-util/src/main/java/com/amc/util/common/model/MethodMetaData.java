package com.amc.util.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 方法的元数据信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MethodMetaData extends BaseMetaData {

    /**
     * 方法返回类型
     */
    private String returnType;

    /**
     * 方法名
     */
    private String name;

    /**
     * 方法的请求参数
     */
    private List<FieldMetaData> parameterList;

    /**
     * 方法的抛出异常名
     */
    private List<String> throwExceptionNames;

    /**
     * 方法的所有注解信息
     */
    private List<AnnotationMetaData> annotationList;


    public MethodMetaData() {
        this.parameterList = new ArrayList<>();
        this.throwExceptionNames = new ArrayList<>();
        this.annotationList = new ArrayList<>();
    }

    public String getSignature() {
        List<String> paramType = parameterList.stream().map(FieldMetaData::getType).collect(Collectors.toList());
        return name + paramType;
    }

}
