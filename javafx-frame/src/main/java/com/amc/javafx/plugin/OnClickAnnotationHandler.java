package com.amc.javafx.plugin;

import com.amc.javafx.annotations.OnClick;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.MethodParser;
import com.amc.javafx.interfaces.ViewPostProcessor;
import com.amc.javafx.model.MethodInfo;
import javafx.scene.control.ButtonBase;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Integer.MIN_VALUE + 2)
public class OnClickAnnotationHandler implements MethodParser, ViewPostProcessor {

    private final Map<String, MethodInfo> onClickActionMap = new HashMap<>();

    @Override
    public void resolve(Class<?> clazz, Method method, String beanName) {
        OnClick onClickAnnotation = method.getAnnotation(OnClick.class);
        if (onClickAnnotation != null) {
            String viewName = method.getName();
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setBeanName(beanName);
            methodInfo.setMethod(method);
            methodInfo.resolveArgs();
            onClickActionMap.put(viewName, methodInfo);
        }
    }

    @Override
    public void postProcessAfterCreate(ViewFactory viewFactory) {
        onClickActionMap.forEach((viewName, methodInfo) -> {
            Object view = viewFactory.getView(viewName);
            if (view instanceof ButtonBase) {
                methodInfo.preExecute(viewFactory);
                ((ButtonBase) view).setOnAction((methodInfo::execute));
            }
        });
    }

}
