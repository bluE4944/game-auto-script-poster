package com.amc.util.common.interfaces;

import java.lang.reflect.Method;

/**
 * 代理类的方法执行逻辑
 * 只要以下方法返回了非空值, 则该值将作为最终的返回值
 */
public interface MethodAdvice {

    default Object before(Object target, Method method, Object[] args) throws Exception {
        return null;
    }

    default Object after(Object result, Object target, Method method, Object[] args) throws Exception {
        return result;
    }

    default Object throwing(Exception exception, Object target, Method method, Object[] args) {
        return null;
    }

}
