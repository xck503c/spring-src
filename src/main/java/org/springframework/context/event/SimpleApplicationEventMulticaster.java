package org.springframework.context.event;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.Executor;

/**
 * 将事件通知给对该事件感兴趣的监听器，而监听器通常会对事件对象进行instance of检查。
 * 默认情况下，事件处理是顺序执行，如果有某个监听器阻塞了，那就会导致后面执行不了；我们也可以指定线程池进行执行，使得
 * 监听器的处理在不同的线程中执行
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster{

    private Executor taskExecutor;

    public SimpleApplicationEventMulticaster(){}

    /**
     * 很奇怪，如果这个设置了，会不会因为Aware接口而被覆盖？如果不会被覆盖那就初始化的顺序，构造在后
     */
    public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
        setBeanFactory(beanFactory);
    }

    /**
     * 默认是SyncTaskExecutor，同步执行，也可以使用异步的方式，如SimpleAsyncTaskExecutor；
     * @see org.springframework.core.task.SyncTaskExecutor
     * @see org.springframework.core.task.SimpleAsyncTaskExecutor
     */
    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected Executor getTaskExecutor() {
        return this.taskExecutor;
    }

    public void multicastEvent(final ApplicationEvent event){
        for (final ApplicationListener listener : getApplicationListeners(event)) {
            Executor executor = getTaskExecutor();
            if (executor != null) {
                executor.execute(new Runnable() {
                    public void run() {
                        listener.onApplicationEvent(event);
                    }
                });
            } else {
                listener.onApplicationEvent(event);
            }
        }
    }
}
