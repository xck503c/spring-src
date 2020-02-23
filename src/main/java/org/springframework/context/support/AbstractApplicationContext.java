package org.springframework.context.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ObjectUtils;

public abstract class AbstractApplicationContext extends DefaultResourceLoader
        implements ConfigurableApplicationContext, DisposableBean {

    private String id;
    private String displayName;
    private ApplicationContext parent; //父容器，组合方式
    private long starupDate; //容器启动时间戳
    private boolean active;
    private boolean closed;
    private final Object activeMonitor; //active状态监视对象，对象锁
    private final Object startupShutdownMonitor; //容器启停的监视对象，对象锁
    private Thread shutdownHook; //当JVM关闭是，自动运行

    public AbstractApplicationContext(){
        this(null);
    }

    public AbstractApplicationContext(ApplicationContext parent){
        this.id = ObjectUtils.identityToString(this);
        this.displayName = ObjectUtils.identityToString(this);
        this.active = false;
        this.closed = false;
        this.activeMonitor = new Object();
        this.startupShutdownMonitor = new Object();
        this.parent = parent;
    }

    //setter and getter
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ApplicationContext getParent() {
        return this.parent;
    }

    public void setParent(ApplicationContext parent) {

    }

    /**
     * 提供默认的关闭钩子注册方法：默认会在JVM关闭时自动调用AbstractApplicationContext的close方法
     */
    @Override
    public void registerShutdownHook(){
        if(shutdownHook == null){
            shutdownHook = new Thread(){
                public void run() {
                    doClose();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    public void destroy() {
        close();
    }

    public void close(){

    }

    protected void doClose(){

    }
}
