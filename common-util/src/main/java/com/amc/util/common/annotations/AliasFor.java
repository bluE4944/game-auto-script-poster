package com.amc.util.common.annotations;

import com.amc.util.common.constants.SimpleConstant;

import java.lang.annotation.*;

/**
 * 为子注解设置属性的注解, 只能用于注解类的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {

    /**
     * 指定子注解的方法名, 默认为本方法名
     */
    String value() default SimpleConstant.STRING;

    /**
     * 指定本注解类的子注解, 默认为本注解类
     */
    Class<? extends Annotation> annotation() default Annotation.class;

}