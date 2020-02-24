package org.springframework.aop.support;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

public class AopUtils {

    /**
     * 判断是否是代理的三个方法，从这里可以看出aop代理本质也是用cglib和jdk动态代理
     * 文章：https://www.cnblogs.com/teach/p/10763845.html
     * 我copy了文中的例子自己手打实验：com.proxy下
     * 静态代理：在编译期就已经确定了代理和被代理的关系
     * 1. 维护成本太高，因为需要为每个需要代理的类编写代理方法
     * 2. 而相比不使用代理，我们使用代理后，可以自由在方法调用前后插入一些日志，计时的打印等
     *
     * JDK动态代理：在方法调用的时候，代理才会被创建，称为动态；而且不需要为每个方法都写一个代理方法
     * 1.动态代理必须要实现一个接口
     * 2. 代理类由Proxy.newProxyInstance动态生成
     *
     * CGLIB动态代理：第三方实现的动态代理库，采用继承代理类的方式，所以不要求一定要有一个接口
     * 1.代理类不能是final
     * 2.Enhancer生成代理类，需要设置被代理类，也就是父类（从这里可以看出是使用继承，生成的子类），设置回调方法
     *
     * 那么怎么个动态法，如何生成的？
     * https://blog.csdn.net/qq_31859365/article/details/82902349
     * 作者真是个人才，不过说实话我以前听到动态代理，都不会想到去探究是怎么个动态，现在倒是变了；
     */
    public static boolean isAopProxy(Object object){
        return object instanceof SpringProxy &&
                (Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass()));
    }

    public static boolean isJdkDynamicProxy(Object object){
        //使用JDK自己的代理判断方法
        return object instanceof SpringProxy && Proxy.isProxyClass(object.getClass());
    }

    public static boolean isCglibProxy(Object object) {
        return object instanceof SpringProxy && ClassUtils.isCglibProxy(object);
    }

    /**
     * 过时了，将具体实现移动到了ClassUtils方法，为了兼容？所以这样写，其他还有几个就先不管了
     */
    @Deprecated
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return ClassUtils.isCglibProxyClass(clazz);
    }

    public static Class<?> getTargetClass(Object candidate){
        Assert.notNull(candidate, "Candidate object must not be null");
        Class<?> result = null;
        if (candidate instanceof TargetClassAware) {
            result = ((TargetClassAware) candidate).getTargetClass();
        }
        if (result == null) {
            result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
        }
        return result;
    }
}
