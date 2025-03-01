package com.amc.javafx.core;

import com.amc.javafx.constants.FxConstant;
import com.amc.javafx.interfaces.StopMethod;
import com.amc.javafx.interfaces.ViewPostProcessor;
import com.amc.javafx.interfaces.ViewRegistry;
import javafx.scene.Node;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewFactory implements SmartInitializingSingleton, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, Object> viewMap = new HashMap<>();
    private PluginManager pluginManager;

    @Override
    public void afterSingletonsInstantiated() {
        viewMap.put(FxConstant.STAGE, applicationContext.getBean(FxConstant.STAGE));
        viewMap.put(FxConstant.ROOT_PANE, applicationContext.getBean(FxConstant.ROOT_PANE));
        List<ViewPostProcessor> viewPostProcessors = pluginManager.getViewPostProcessors();
        List<ViewRegistry> viewRegistries = pluginManager.getViewRegistries();
        viewPostProcessors.forEach(viewPostProcessor -> viewPostProcessor.postProcessBeforeCreate(this));
        viewRegistries.forEach(viewRegistry -> viewRegistry.register(this));
        viewPostProcessors.forEach(viewPostProcessor -> viewPostProcessor.postProcessAfterCreate(this));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        pluginManager = applicationContext.getBean(PluginManager.class);
    }

    protected void doStop() {
        List<StopMethod> stopMethods = pluginManager.getStopMethods();
        stopMethods.forEach(stopMethod -> stopMethod.execute(this));
    }

    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

    public Object getView(String viewName) {
        return viewMap.get(viewName);
    }

    public <T> T getView(String viewName, Class<T> viewClass) {
        return (T) getView(viewName);
    }

    public Object getArgVal(String argName) {
        Object result = getView(argName);
        if (result == null) {
            result = getBean(argName);
        }
        return result;
    }

    public void registerView(String viewName, Object view) {
        viewMap.put(viewName, view);
    }

    public void registerViews(Map<String, Node> views) {
        if (views != null) {
            viewMap.putAll(views);
        }
    }

}
