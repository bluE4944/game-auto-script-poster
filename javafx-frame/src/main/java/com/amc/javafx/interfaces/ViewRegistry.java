package com.amc.javafx.interfaces;

import com.amc.javafx.core.ViewFactory;

/**
 * View的创建与注册
 */
public interface ViewRegistry {

    void register(ViewFactory viewFactory);

}
