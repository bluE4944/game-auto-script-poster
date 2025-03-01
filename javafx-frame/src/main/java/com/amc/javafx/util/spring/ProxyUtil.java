package com.amc.javafx.util.spring;

import net.sf.cglib.proxy.Enhancer;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * 基于spring的代理工具
 */
public class ProxyUtil {

    public static Object getJdkProxy(Object target, MethodInterceptor methodInterceptor) {
        return getTargetProxy(target, methodInterceptor, false);
    }

    public static Object getJdkProxy(Class<?> proxyClass, MethodInterceptor methodInterceptor) {
        return getInterfaceProxy(proxyClass, methodInterceptor, false);
    }

    public static Object getCglibProxy(Object target, MethodInterceptor methodInterceptor) {
        return getTargetProxy(target, methodInterceptor, true);
    }

    public static Object getCglibProxy(Class<?> proxyClass, MethodInterceptor methodInterceptor) {
        return getInterfaceProxy(proxyClass, methodInterceptor, true);
    }

    private static Object getTargetProxy(Object target, MethodInterceptor methodInterceptor, boolean proxyTargetClass) {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(methodInterceptor);
        proxyFactory.setProxyTargetClass(proxyTargetClass);
        proxyFactory.setExposeProxy(true);
        return proxyFactory.getProxy();
    }

    private static Object getInterfaceProxy(Class<?> proxyClass, MethodInterceptor methodInterceptor, boolean proxyTargetClass) {
        Class<?>[] interfaces = proxyClass.isInterface() ? new Class[] {proxyClass} : proxyClass.getInterfaces();
        ProxyFactory proxyFactory = new ProxyFactory(interfaces);
        proxyFactory.addAdvice(methodInterceptor);
        proxyFactory.setProxyTargetClass(proxyTargetClass);
        return proxyFactory.getProxy();
    }

    public static Object getCglibProxy0(Object target, MethodInterceptor methodInterceptor) {
        return Enhancer.create(target.getClass(), (net.sf.cglib.proxy.MethodInterceptor) (proxy, method, args, methodProxy) -> {
            MethodInvocation methodInvocation = new MethodInvocation() {
                @Override
                public Object proceed() throws Throwable {
                    return methodProxy.invokeSuper(proxy, args);
                }
                @Override
                public Object getThis() {
                    return target;
                }
                @Override
                public AccessibleObject getStaticPart() {
                    return method;
                }
                @Override
                public Object[] getArguments() {
                    return args;
                }
                @Override
                public Method getMethod() {
                    return method;
                }
            };
            return methodInterceptor.invoke(methodInvocation);
        });
    }

}
