package com.amc.javafx.model;

import com.amc.javafx.constants.FxConstant;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.util.spring.CommonUtil;
import com.amc.util.reflect.ReflectUtil;
import javafx.event.Event;
import javafx.stage.Stage;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
public class MethodInfo {

    private String beanName;

    private Method method;

    private List<String> argNames;

    public void resolveArgs() {
        argNames = new ArrayList<>();
        if (method.getParameterCount() > 0) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            List<String> paramNames = null;

            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (ReflectUtil.isInstanceof(type, Stage.class)) {
                    argNames.add(FxConstant.STAGE);
                }
                else if (ReflectUtil.isInstanceof(type, Event.class)) {
                    argNames.add(FxConstant.EVENT);
                }
                else {
                    if (paramNames == null) {
                        paramNames = CommonUtil.getMethodParamNames(method);
                    }
                    argNames.add(paramNames.get(i));
                }
            }
        }
    }

    private Object bean;
    private List<Object> methodArgs;

    public void preExecute(ViewFactory viewFactory) {
        if (bean == null) {
            bean = viewFactory.getBean(beanName);
            methodArgs = new ArrayList<>();
            for (String argName : argNames) {
                methodArgs.add(viewFactory.getArgVal(argName));
            }
        }
    }

    public Object execute() {
        return ReflectUtil.invokeMethod(bean, method, methodArgs.toArray());
    }

    public Object execute(Event event) {
        try {
            List<Object> argValList = new ArrayList<>();
            for (int i = 0; i < argNames.size(); i++) {
                String argName = argNames.get(i);
                Object argVal = FxConstant.EVENT.equals(argName) ? event : methodArgs.get(i);
                argValList.add(argVal);
            }
            ReflectUtil.invokeMethod(bean, method, argValList.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
