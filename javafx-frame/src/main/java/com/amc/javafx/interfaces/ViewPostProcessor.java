package com.amc.javafx.interfaces;

import com.amc.javafx.core.ViewFactory;

/**
 * ViewRegistry的后置处理器
 */
public interface ViewPostProcessor {

    default void postProcessBeforeCreate(ViewFactory viewFactory) {

    }

    default void postProcessAfterCreate(ViewFactory viewFactory) {

    }

}