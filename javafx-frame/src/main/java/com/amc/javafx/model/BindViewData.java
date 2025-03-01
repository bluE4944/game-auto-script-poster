package com.amc.javafx.model;

import lombok.Data;

import java.lang.reflect.Field;

@Data
public class BindViewData {

    private String beanName;

    private Field beanField;

}
