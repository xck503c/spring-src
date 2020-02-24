package com.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MyMethodInterceptor implements MethodInterceptor {

    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("开始CGLib动态代理" + method.getName());
        Object object=methodProxy.invokeSuper(o, args);
        System.out.println("结束CGLib动态代理" + method.getName());
        return object;
    }
}
