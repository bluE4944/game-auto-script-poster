package com.amc.util.common.model;

import lombok.Data;

import java.util.List;

/**
 * 泛型信息
 */
@Data
public class GenericType {

    /**
     * 类型
     */
    private Class<?> clazz;

    /**
     * 泛型集合
     */
    private List<GenericType> genericTypes;

    /**
     * 签名
     */
    private String signature;

}
