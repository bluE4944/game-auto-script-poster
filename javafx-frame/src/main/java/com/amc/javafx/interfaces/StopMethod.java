package com.amc.javafx.interfaces;

import com.amc.javafx.core.ViewFactory;

/**
 * Stop注解相关动作
 */
public interface StopMethod extends MethodParser {

    void execute(ViewFactory viewFactory);

}
