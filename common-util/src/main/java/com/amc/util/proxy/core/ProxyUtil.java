package com.amc.util.proxy.core;

import com.amc.util.common.interfaces.MethodAdvice;
import com.amc.util.proxy.ProxyFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;

/**
 * 代理类加成工具
 */
public class ProxyUtil {

    /**
     * JDK动态代理
     */
    public static Object getJdkProxy(ProxyFactory proxyFactory) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Class<?>[] interfaces = proxyFactory.getInterfaces();
        InvocationHandler invocationHandler = getJdkHandler(proxyFactory);
        return Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
    }

    /**
     * Cglib动态代理
     */
    public static Object getCglibProxy(ProxyFactory proxyFactory) {
        Class<?> targetClass = getProxyClasses(proxyFactory)[0];
        MethodInterceptor methodInterceptor = getCglibHandler(proxyFactory);
        return Enhancer.create(targetClass, methodInterceptor);
    }

    private static InvocationHandler getJdkHandler(ProxyFactory proxyFactory) {
        Object target = proxyFactory.getTarget();
        MethodAdvice methodAdvice = proxyFactory.getMethodAdvice();

        return (proxy, method, args) -> {
            Object result = isSkipMethod(proxyFactory, method, args);
            if (Objects.nonNull(result)) {
                return result;
            }
            try {
                result = methodAdvice.before(target, method, args);
                if (Objects.nonNull(result)) {
                    return result;
                }
                if (Objects.nonNull(target)) {
                    result = method.invoke(target, args);
                }
                return methodAdvice.after(result, target, method, args);
            } catch (Exception e) {
                result = methodAdvice.throwing(e, target, method, args);
                if (Objects.nonNull(result)) {
                    return result;
                }
                throw e;
            }
        };
    }

    private static MethodInterceptor getCglibHandler(ProxyFactory proxyFactory) {
        Object target = proxyFactory.getTarget();
        MethodAdvice methodAdvice = proxyFactory.getMethodAdvice();

        return (proxy, method, args, methodProxy) -> {
            Object result = isSkipMethod(proxyFactory, method, args);
            if (Objects.nonNull(result)) {
                return result;
            }
            try {
                result = methodAdvice.before(target, method, args);
                if (Objects.nonNull(result)) {
                    return result;
                }
                if (Objects.nonNull(target)) {
                    // 即使方法之间内部调用, 依然会走代理逻辑
                    result = methodProxy.invokeSuper(proxy, args);
                }
                return methodAdvice.after(result, target, method, args);
            } catch (Exception e) {
                result = methodAdvice.throwing(e, target, method, args);
                if (Objects.nonNull(result)) {
                    return result;
                }
                throw e;
            }
        };
    }

    private static Object isSkipMethod(ProxyFactory proxyFactory, Method method, Object[] args) {
        Object target = proxyFactory.getTarget();
        String methodName = method.getName();
        int parameterCount = method.getParameterCount();

        if (Objects.equals(methodName, "equals") && parameterCount == 1 && method.getParameterTypes()[0] == Object.class) {
            return Objects.nonNull(target) ? target.equals(args[0]) : proxyFactory.equals(args[0]);
        }
        if (Objects.equals(methodName, "hashCode") && parameterCount == 0) {
            return Objects.nonNull(target) ? target.hashCode() : proxyFactory.hashCode();
        }
        if (Objects.equals(methodName, "toString") && parameterCount == 0) {
            return "Proxy$" + (Objects.nonNull(target) ? target.toString() : Arrays.asList(getProxyClasses(proxyFactory)));
        }
        return null;
    }

    private static Class<?>[] getProxyClasses(ProxyFactory proxyFactory) {
        Object target = proxyFactory.getTarget();
        return Objects.nonNull(target) ? new Class[] {target.getClass()} : proxyFactory.getInterfaces();
    }

}
