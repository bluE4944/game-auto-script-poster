package com.amc.javafx.plugin;

import com.amc.javafx.annotations.Stop;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.StopMethod;
import com.amc.javafx.model.MethodInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(Integer.MIN_VALUE + 3)
public class StopAnnotationHandler implements StopMethod {

    private final List<MethodInfo> stopActionList = new ArrayList<>();

    @Override
    public void resolve(Class<?> clazz, Method method, String beanName) {
        Stop stopAnnotation = method.getAnnotation(Stop.class);
        if (stopAnnotation != null) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setBeanName(beanName);
            methodInfo.setMethod(method);
            methodInfo.resolveArgs();
            stopActionList.add(methodInfo);
        }
    }

    @Override
    public void execute(ViewFactory viewFactory) {
        stopActionList.forEach(methodInfo -> {
            methodInfo.preExecute(viewFactory);
            methodInfo.execute();
        });
    }

}