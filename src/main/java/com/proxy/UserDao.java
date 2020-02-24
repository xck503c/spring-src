package com.proxy;

/**
 * 例子来源，博客：https://www.cnblogs.com/teach/p/10763845.html
 * 被代理的类
 */
public class UserDao implements IUserDao<String>{
    @Override
    public void save() {
        System.out.println("save");
    }

    @Override
    public void find() {
        System.out.println("find");
    }
}
