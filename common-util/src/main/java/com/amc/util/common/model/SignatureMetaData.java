package com.amc.util.common.model;

import lombok.Data;

import java.util.*;

/**
 * 类的泛型元数据信息
 */
@Data
public class SignatureMetaData {

    /**
     * 类的泛型
     */
    private List<String> generics;

    /**
     * 类的泛型默认实类
     */
    private Map<String, String> genericClass;

    /**
     * 父类名
     */
    private String superName;

    /**
     * 父类的泛型实类
     */
    private List<String> superGenericClass;


    public SignatureMetaData() {
        this.generics = new ArrayList<>();
        this.genericClass = new LinkedHashMap<>();
        this.superGenericClass = new ArrayList<>();
    }

}
