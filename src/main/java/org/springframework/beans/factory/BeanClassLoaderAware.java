package org.springframework.beans.factory;

/**
 * 实现该接口的bean可以拿到bean工厂锁加载bean的类加载器
 * 该接口主要有框架类实现：
 * 1. 用于判断某个类是否是被该类加载器加载？ 比如事件的缓存判断
 */
public interface BeanClassLoaderAware {
    void setBeanClassLoader(ClassLoader classLoader);
}
