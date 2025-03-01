package com.amc.javafx.annotations;

import com.amc.javafx.constants.FxConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加了该注解的方法, 会作为视图组件
 * 视图名为方法名
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface View {

    /**
     * 父组件名
     */
    String parent() default FxConstant.ROOT_PANE;

    /**
     * 加载顺序
     */
    int order() default Integer.MAX_VALUE;

}
