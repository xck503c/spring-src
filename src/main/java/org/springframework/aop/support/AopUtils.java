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
     * --------------------------------------------
     * 那么怎么个动态法，如何生成的？具体可以看下面两篇文章，然后自己动手
     * https://blog.csdn.net/qq_31859365/article/details/82902349
     * https://zhuanlan.zhihu.com/p/93949583
     * 前面那篇实现思路很奇特，作者真是个人才。
     * 反编译后的代理类放到com.proxy.ProxyTest中，看完了生成的类后，我们接下来需要探究这个是如何生成的
     * 生成的代码是：
     * byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
     *                 proxyName, interfaces, accessFlags);
     * 1. addProxyMethod方法：
     * (1) 针对一个方法来说，我们需要知道方法名，方法参数类型，返回值类型，抛出异常的类型
     * (2) 根据方法名，方法参数类型，我们要拿到方法签名作为Map的key，形如：equals(Ljava/lang/Object;)
     * (3) 最后生成ProxyMethod对象，放入其中，它里面包含的属性有：
     * 除了(1)中说的4个还有该方法在代理类中的字段名，这块用的是自动生成递增的方式
     * 然后就是来自哪个类，以被代理类为例，因为不知道被代理类具体类型是什么，所以这个就是接口的Class；
     * 2. 后面就是构建DataOutPutStream，生成最终的Class字节流，然后用类加载器加载到内存中，就可以使用了
     * 具体细节，这里就不关心了；
     * 3. 最后会对其进行缓存，咋缓存以后再探究
     *
     * 特点：
     * 1. jdk动态代理基于接口实现，并且会继承Proxy类，而不采用继承被代理类的方式：
     * 不采用继承而采用基于接口的方式，具体可以看第二篇文章：
     * (1) 如果类恰巧是final，那不就不行了
     * (2) 代理类对请求的处理本质是：拦截和转发，最终执行还是要交给被代理类，所以无端继承字段会浪费空间
     * 2. 除了需要代理的方法，还额外代理了Object中的hashCode，toString，equals
     * 3. 通过反射来重新构建代理Class，并且进行缓存
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
