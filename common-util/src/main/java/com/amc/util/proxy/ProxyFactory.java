package com.amc.util.proxy;

import com.amc.util.common.interfaces.MethodAdvice;
import com.amc.util.proxy.core.ProxyUtil;
import lombok.Data;

/**
 * 代理类的元数据信息
 */
@Data
public class ProxyFactory {

    /**
     * 原型类
     */
    private Object target;

    /**
     * 代理接口
     */
    private Class<?>[] interfaces;

    /**
     * 方法切面逻辑
     */
    private MethodAdvice methodAdvice;


    public ProxyFactory(Object target) {
        this.target = target;
        this.interfaces = target.getClass().getInterfaces();
    }
    public ProxyFactory(Class<?>... interfaces) {
        this.interfaces = interfaces;
    }

    public Object getAutoProxy() {
        return interfaces.length > 0 ? getJdkProxy(Object.class) : getCglibProxy(Object.class);
    }
    public <T> T getJdkProxy(Class<T> clazz) {
        return (T) ProxyUtil.getJdkProxy(this);
    }
    public <T> T getCglibProxy(Class<T> clazz) {
        return (T) ProxyUtil.getCglibProxy(this);
    }

}
