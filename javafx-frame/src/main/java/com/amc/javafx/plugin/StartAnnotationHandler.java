package com.amc.javafx.plugin;

import com.amc.javafx.annotations.Start;
import com.amc.javafx.constants.FxConstant;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.MethodParser;
import com.amc.javafx.interfaces.ViewPostProcessor;
import com.amc.javafx.model.MethodInfo;
import com.amc.javafx.model.WindowInfo;
import javafx.scene.layout.Pane;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(Integer.MIN_VALUE + 1)
public class StartAnnotationHandler implements MethodParser, ViewPostProcessor {

    private final List<MethodInfo> startActionList = new ArrayList<>();

    @Override
    public void resolve(Class<?> clazz, Method method, String beanName) {
        Start startAnnotation = method.getAnnotation(Start.class);
        if (startAnnotation != null) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setBeanName(beanName);
            methodInfo.setMethod(method);
            methodInfo.resolveArgs();
            startActionList.add(methodInfo);
        }
    }

    @Override
    public void postProcessBeforeCreate(ViewFactory viewFactory) {
        startActionList.forEach(methodInfo -> {
            methodInfo.preExecute(viewFactory);
            methodInfo.execute();
        });

        Pane rootPane = viewFactory.getView(FxConstant.ROOT_PANE, Pane.class);
        WindowInfo windowInfo = viewFactory.getBean(WindowInfo.class);
        rootPane.setPrefSize(windowInfo.getWidth(), windowInfo.getHeight());
    }

}