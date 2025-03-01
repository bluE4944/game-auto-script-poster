package com.amc.javafx.plugin;

import com.amc.javafx.annotations.View;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.MethodParser;
import com.amc.javafx.interfaces.ViewRegistry;
import com.amc.javafx.model.MethodInfo;
import com.amc.javafx.model.ViewData;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
@Order(Integer.MIN_VALUE + 2)
public class ViewAnnotationHandler implements MethodParser, ViewRegistry {

    private final List<ViewData> viewDataList = new ArrayList<>();
    private final Map<String, ViewData> viewDataMap = new HashMap<>();
    private final Set<String> creatingView = new HashSet<>();
    private ViewFactory viewFactory;

    @Override
    public void resolve(Class<?> clazz, Method method, String beanName) {
        View viewAnnotation = method.getAnnotation(View.class);
        if (viewAnnotation != null) {
            String viewName = method.getName();
            String parentName = viewAnnotation.parent();
            int order = viewAnnotation.order();
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setBeanName(beanName);
            methodInfo.setMethod(method);
            methodInfo.resolveArgs();

            ViewData viewData = new ViewData();
            viewData.setName(viewName);
            viewData.setParent(parentName);
            viewData.setOrder(order);
            viewData.setMethodInfo(methodInfo);
            viewDataList.add(viewData);
        }
    }

    @Override
    public void register(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        viewDataList.sort(Comparator.comparingInt(ViewData::getOrder));
        List<String> names = new ArrayList<>();
        for (ViewData viewData : viewDataList) {
            String viewName = viewData.getName();
            names.add(viewName);
            viewDataMap.put(viewName, viewData);
        }
        names.forEach(this::getView);
    }

    private Object getView(String viewName) {
        Object result = viewFactory.getView(viewName);
        return result != null ? result : createView(viewName);
    }

    private Object createView(String viewName) {
        if (!viewDataMap.containsKey(viewName)) {
            throw new RuntimeException("无法找到视图: " + viewName);
        }
        if (!creatingView.add(viewName)) {
            throw new RuntimeException("出现循环引用: " + viewName);
        }

        ViewData viewData = viewDataMap.get(viewName);
        MethodInfo methodInfo = viewData.getMethodInfo();
        methodInfo.preExecute(viewFactory);
        Object newView = methodInfo.execute();
        if (newView != null) {
            bindParentView(newView, viewData);
            viewFactory.registerView(viewName, newView);
        }

        creatingView.remove(viewName);
        return newView;
    }

    private void bindParentView(Object view, ViewData viewData) {
        if (view instanceof Node) {
            Object parentView = getView(viewData.getParent());
            if (parentView instanceof Pane) {
                ((Pane) parentView).getChildren().add((Node) view);
            }
        }
    }

}
