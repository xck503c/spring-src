package com.proxy;

/**
 * 例子来源，博客：https://www.cnblogs.com/teach/p/10763845.html
 * 代理类和被代理类的公共接口
 */
public interface IUserDao<T extends String>{
    void save();

    void find();
}
