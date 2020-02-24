package com.proxy;

import net.sf.cglib.proxy.Enhancer;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Proxy;

/**
 * 例子来源，博客：https://www.cnblogs.com/teach/p/10763845.html
 * 测试方法
 */
public class ProxyMainTest {
    public static void main(String[] args) {
        //静态代理
        UserDao userDao = new UserDao();
        UserDaoStaticProxy userDaoStaticProxy = new UserDaoStaticProxy(userDao);
        userDaoStaticProxy.save();
        userDaoStaticProxy.find();

        System.out.println("-------------------------");
        //jdk动态代理
        DynamicProxy dynamicProxy = new DynamicProxy(userDao);
        IUserDao iud = (IUserDao)Proxy.newProxyInstance(userDao.getClass().getClassLoader(), userDao.getClass().getInterfaces(),
                dynamicProxy);
        iud.save();
        iud.find();

        System.out.println("-------------------------");
        //cglib动态代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserDao.class);
        enhancer.setCallback(new MyMethodInterceptor());
        UserDao cglibUserDao = (UserDao)enhancer.create();
        cglibUserDao.save();
        cglibUserDao.find();

        System.out.println("cglib动态代理类名:" + cglibUserDao.getClass().getName());

        //null
        System.out.println(GenericTypeResolver.resolveTypeArgument(iud.getClass(), IUserDao.class));
        //class java.lang.String
        System.out.println(GenericTypeResolver.resolveTypeArgument(cglibUserDao.getClass(), IUserDao.class));
        //class java.lang.String
        System.out.println(GenericTypeResolver.resolveTypeArgument(cglibUserDao.getClass().getSuperclass(), IUserDao.class));

        //代理操作save，开启事务
        //save
        //代理操作save，关闭事务
        //代理操作find，开启事务
        //find
        //代理操作find，关闭事务
        //-------------------------
        //jdk动态代理save
        //save
        //jdk动态代理save
        //jdk动态代理find
        //find
        //jdk动态代理find
        //-------------------------
        //开始CGLib动态代理save
        //save
        //结束CGLib动态代理save
        //开始CGLib动态代理find
        //find
        //结束CGLib动态代理find
        //cglib动态代理类名:com.proxy.UserDao$$EnhancerByCGLIB$$b9f787e2
    }
}
