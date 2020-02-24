package com.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxy implements InvocationHandler {
    private IUserDao iud = null;

    public DynamicProxy(IUserDao iud) {
        this.iud = iud;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        System.out.println("jdk动态代理" + method.getName());
//        System.out.println(proxy.toString()); //这里调用会造成死循环，说明proxy是代理对象
        method.invoke(iud, args);
        System.out.println("jdk动态代理" + method.getName());
        return result;
    }
}
