package com.amc.javafx.model;

import lombok.Data;

@Data
public class ViewData {

    private String name;

    private String parent;

    private Integer order;

    private MethodInfo methodInfo;

}
