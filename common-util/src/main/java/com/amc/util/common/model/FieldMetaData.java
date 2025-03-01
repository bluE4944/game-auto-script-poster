package com.amc.util.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性的元数据信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FieldMetaData extends BaseMetaData {

    /**
     * 属性类型
     */
    private String type;

    /**
     * 属性名
     */
    private String name;

    /**
     * 属性的所有注解信息
     */
    private List<AnnotationMetaData> annotationList;


    public FieldMetaData() {
        this.annotationList = new ArrayList<>();
    }

}
