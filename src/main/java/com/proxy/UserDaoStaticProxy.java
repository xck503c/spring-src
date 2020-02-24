package com.proxy;

/**
 * 例子来源，博客：https://www.cnblogs.com/teach/p/10763845.html
 * 静态代理类
 */
public class UserDaoStaticProxy implements IUserDao{
    private IUserDao iUserDao = null;

    public UserDaoStaticProxy(IUserDao userDao){
        this.iUserDao = userDao;
    }

    @Override
    public void save() {
        if(iUserDao!=null){
            System.out.println("代理操作save，开启事务");
            iUserDao.save();
            System.out.println("代理操作save，关闭事务");
        }
    }

    @Override
    public void find() {
        if(iUserDao!=null){
            System.out.println("代理操作find，开启事务");
            iUserDao.find();
            System.out.println("代理操作find，关闭事务");
        }
    }
}
