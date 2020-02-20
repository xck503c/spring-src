package org.springframework.context;


import org.springframework.beans.BeansException;

/**
 * https://zhuanlan.zhihu.com/p/92799996
 *
 * 可配置的 ApplicationContext
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle{

    //设置应用程序上下文的ID
    void setId(String id);

    void setParent(ApplicationContext parent);

    /**
     * 容器的启动方法，加载或者刷新一个持久化配置，可能是XML文件，properties文件或者其他
     */
    void refresh() throws BeansException, IllegalStateException;

    /**
     * 向JVM注册一个关闭钩子，随着JVM关闭而调用
     */
    void registerShutdownHook();

    /**
     * 关闭应用程序上下文，释放实现可能持有的所有资源和锁
     */
    void close();

    /**
     * 是否还存活
     */
    boolean isActive();
}
