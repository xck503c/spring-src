package org.springframework.util;

import java.io.Serializable;

/**
 * Simple customizable helper class，用来创建新线程实例
 * Provides various bean properties：线程名前缀，线程优先级等等
 * 是线程工厂的基类：org.springframework.scheduling.concurrent.CustomizableThreadFactory
 * 1. 每个工厂都有自己的线程组，所有创建的线程都属于这个组
 */
public class CustomizableThreadCreator implements Serializable {

    private String threadNamePrefix;

    private int threadPriority = Thread.NORM_PRIORITY;

    private boolean daemon = false;

    private ThreadGroup threadGroup;

    private int threadCount = 0;

    private final Object threadCountMonitor = new SerializableMonitor();

    public CustomizableThreadCreator(){
        this.threadNamePrefix = getDefaultThreadNamePrefix();
    }

    public CustomizableThreadCreator(String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public int getThreadPriority() {
        return this.threadPriority;
    }

    /**
     * https://www.cnblogs.com/qq1290511257/p/10645106.html
     * 守护线程：
     * 1. Java中的线程可以分为：守护线程和用户线程；其中GC就是典型的守护线程，普通线程就是用户线程
     * 2. 守护线程是所有普通线程的保姆，当没有普通线程存在时，那守护线程也没有存在的必要，就会随着JVM一起结束工作；
     * 自己可以手动试一下，创建一个线程里面循环执行，main线程执行完毕后，依然不会结束，但是如果设置守护，则如果main
     * 线程结束后，那守护线程也会结束，最后整个JVM就结束了；
     * 所以只要不是kill了整个进程，那程序会等所有线程结束后自动退出
     * 3. 使用的时候就要根据它的特点来：举例：文件读写，数据库操作等就不能交给它做；
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isDaemon() {
        return this.daemon;
    }

    public void setThreadGroupName(String name) {
        this.threadGroup = new ThreadGroup(name);
    }

    /**
     * 线程组：
     * 1. 每个线程组都管理了一组线程，同时也可以管理多个子线程组，有了父子关系后，自然也有父线程组
     * ，但是父线程组只能有一个
     * 2.整个是呈树形状
     */
    public void setThreadGroup(ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    public Thread createThread(Runnable runnable){
        Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
        thread.setPriority(getThreadPriority());
        thread.setDaemon(isDaemon());
        return thread;
    }

    protected String nextThreadName(){
        int threadNumber = 0;
        synchronized (this.threadCountMonitor) {
            this.threadCount++;
            threadNumber = this.threadCount;
        }
        return getThreadNamePrefix() + threadNumber;
    }

    //类名
    protected String getDefaultThreadNamePrefix(){
        return ClassUtils.getShortName(getClass()) + "-";
    }

    /**
     * Empty class used for a serializable monitor object.
     */
    private static class SerializableMonitor implements Serializable {}
}
