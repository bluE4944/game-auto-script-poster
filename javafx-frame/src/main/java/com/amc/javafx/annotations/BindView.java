package com.amc.javafx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加了该注解的属性, 会与对应视图做双向绑定
 * 视图名为属性名
 * 遇到多重代理对象时, 模型可能为null, 需要将模型对象设为静态对象
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindView {

}
