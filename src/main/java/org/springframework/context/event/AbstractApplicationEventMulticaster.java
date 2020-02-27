package org.springframework.context.event;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供基本监听器注册工具，它的实现类是SimpleApplicationEventMulticaster
 *
 * 1. 提供了默认的检索器，默认的检索器是缓存数据的来源，也就是说如果没有这个缓存，需要缓存的时候，需要从这里面
 * 遍历，取出符合条件的进行缓存
 * 2. 缓存是一个Map，缓存了支持该事件类型和事件源的所有监听器实例和beanName
 * (1) 不缓存不是bean工厂所属类加载器的事件，至于为啥，不知道
 * (2) 如果不是，则会每次获取的时候都会重新检索一次
 * (3) 获取缓存时，一次通过并发Map访问，一次通过互斥变量访问
 * 3. 为了可以支持缓存，有一个Smart监听接口，以及该接口的默认实现(Smart适配器)
 * (1)判断的方法，就是遍历，获取父泛型接口和父泛型类，这里涉及到Type接口以及其子类，涉及到原始类型和泛型类型的问题，
 * 延伸出：泛型擦除，为什么会出现伪泛型，以及一些Type的术语定义等等
 * (2)同样为了判断是否支持，还涉及到代理，spring的aop代理，cglib代理等
 * 看了几个类就涉及这么多。。。
 */
public abstract class AbstractApplicationEventMulticaster
        implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {

    /**
     * 默认的检索者，储存着所需的所有监听器和beanName，它通过直接调用add相关方法添加
     * 同时也是缓存中数据的来源
     */
    private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

    //retriever缓存
    private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache =
            new ConcurrentHashMap<>();

    private ClassLoader beanClassLoader;

    private BeanFactory beanFactory;

    //以默认检索作为互斥变量，保护该容器的添加操作
    private Object retrievalMutex = this.defaultRetriever;

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory){
        this.beanFactory = beanFactory;
        //...其他
    }

    private BeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
                    "because it is not associated with a BeanFactory");
        }
        return this.beanFactory;
    }

    @Override
    public void addApplicationListener(ApplicationListener listener) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.add(listener);
            this.retrieverCache.clear(); //检索来源变了，缓存也就失效了
        }
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    @Override
    public void removeApplicationListener(ApplicationListener listener) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.remove(listener);
            this.retrieverCache.clear();
        }
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.clear();
            this.defaultRetriever.applicationListenerBeans.clear();
            this.retrieverCache.clear();
        }
    }

    protected Collection<ApplicationListener> getApplicationListeners(){
        synchronized (retrievalMutex){ //防止变化
            return this.defaultRetriever.getApplicationListeners();
        }
    }

    /**
     * 首先这是一个protected方法，说明这是可以给子类内部共享但是不能给外人使用的
     * 其次，该方法作用就是根据给定的event类型以及event中的事件源类型，获得对应的监听器；
     * 那么是如何获得？
     */
    protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event){
        //1.获取事件类型和事件源类型，并创建缓存key
        Class<? extends ApplicationEvent> eventType = event.getClass();
        Object source = event.getSource();
        Class<?> sourceType = (source != null ? source.getClass() : null);
        ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

        //z. 直接去缓存中找，源码注释称为快速确认，该过程不需要同步，所以称为快速
        ListenerRetriever retriever = retrieverCache.get(cacheKey);
        if(retriever!=null){
            return retriever.getApplicationListeners();
        }

        //因为不是通过beanClassLoader加载所以就不需要缓存？为啥
        if(beanClassLoader == null
                || (ClassUtils.isCacheSafe(eventType, beanClassLoader) &&
                    (sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))){
            //同步构建并缓存ListenerRetriever
            synchronized (retrievalMutex){
                //再次同步获取
                retriever = retrieverCache.get(cacheKey);
                if(retriever!=null){
                    return retriever.getApplicationListeners();
                }
                retriever = new ListenerRetriever(true);
                //去默认检索器中检索监听器
                Collection<ApplicationListener> listeners =
                        retrieveApplicationListeners(eventType, sourceType, retriever);
                //放入缓存
                this.retrieverCache.put(cacheKey, retriever);
                return listeners;
            }
        }else {
            //不需要缓存，直接检索
            return retrieveApplicationListeners(eventType, sourceType, null);
        }
    }

    /**
     * 真正检索操作，通过给定事件类型和事件源类型来检索，返回符合条件的所有监听器，若有传入储存的检索器，则添加用于缓存
     * @param eventType 事件类型
     * @param sourceType 事件源类型
     * @param retriever 如果想要缓存检索结果，传入
     * @return
     */
    private Collection<ApplicationListener> retrieveApplicationListeners(
            Class<? extends ApplicationEvent> eventType, Class<?> sourceType, ListenerRetriever retriever){
        //储存检索结构
        LinkedList<ApplicationListener> allListeners = new LinkedList<>();

        //同步获取检索数据来源，copy一份
        Set<ApplicationListener> listeners;
        Set<String> listenerBeans;
        synchronized (retrievalMutex){
            listeners = new LinkedHashSet<>(defaultRetriever.applicationListeners);
            listenerBeans = new LinkedHashSet<>(defaultRetriever.applicationListenerBeans);
        }

        //检索支持事件类型的监听器
        for(ApplicationListener listener : listeners){
            if(supportsEvent(listener, eventType, sourceType)){
                if(retriever != null){
                    allListeners.add(listener);
                }
                allListeners.add(listener);
            }
        }

        //根据监听器bean name，检索支持事件类型的监听器
        if(!listenerBeans.isEmpty()){
            BeanFactory beanFactory = getBeanFactory();
            for(String listenerBeanName : listenerBeans){
                ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                //不包含且支持
                if(!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)){
                    if (retriever != null) {
                        retriever.applicationListenerBeans.add(listenerBeanName);
                    }
                    allListeners.add(listener);
                }
            }
        }

        return allListeners;
    }

    /**
     * 确定该监听器，是否支持给定的事件；
     * 1. 默认情况下，查看是否实现了martApplicationListener接口；
     * 2. 如果是实现标准接口的，则使用通用(generic)监听适配器GenericApplicationListenerAdapter来检测；
     * 检测的职责是是可以自己实现(spring提供了接口)，而且就算没有默认实现也提供了一个叫做适配器的东西，适配检测
     * 这个适配器：提供了默认的检测方法；
     * @param listener 要检查的对象
     * @param eventType 要检查的事件类型
     * @param sourceType 要检查的事件源
     * @return
     */
    protected boolean supportsEvent(
            ApplicationListener listener, Class<? extends ApplicationEvent> eventType, Class<?> sourceType){
        SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener) ?
                (SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener);
        return smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType);
    }

    /**
     * 静态内部类和非静态内部类使用场景，感觉这个解释还是不妥：
     * https://www.jianshu.com/p/c2c54664d4c9
     * 1. 外部类与内部类有很强的联系
     * 2. 内部类可以单独创建
     * 3. 保持嵌套可读性
     */
    /**
     * 基于事件类型，事件源的ListenerRetriever缓存key
     */
    private static class ListenerCacheKey{
        private final Class<?> eventType;

        private final Class<?> sourceType;

        public ListenerCacheKey(Class<?> eventType, Class<?> sourceType){
            this.eventType = eventType;
            this.sourceType = sourceType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            ListenerCacheKey otherKey = (ListenerCacheKey) other;
            return ObjectUtils.nullSafeEquals(this.eventType, otherKey.eventType) &&
                    ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType);
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.eventType) * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
        }
    }


    /**
     * 特定类型监听器的检索者
     * Helper class, 封装(encapsulates)一组特定的监听器；
     * 1. 这是一个事件监听管理器的帮助类，所以是private
     * 2. 每个特定的事件类型 (ApplicationEvent的class类型) 和
     * 事件源 (该事件下的事件源) 都对应一个helper实例
     */
    private class ListenerRetriever {
        public final Set<ApplicationListener> applicationListeners;

        public final Set<String> applicationListenerBeans;

        //private final boolean preFiltered; //不理解这个有啥用

        public ListenerRetriever(boolean preFiltered){
            this.applicationListeners = new HashSet<ApplicationListener>();
            this.applicationListenerBeans = new HashSet<String>();
            //this.preFiltered = preFiltered;
        }

        public Collection<ApplicationListener> getApplicationListeners(){
            LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
            //先将储存的全部放进去
            for(ApplicationListener listener : allListeners){
                allListeners.add(listener);
            }
            if(!applicationListenerBeans.isEmpty()){
                //获取bean工厂，遍历缓存的beanName，拿到bean
                for(String listenerBeanName : this.applicationListenerBeans){
                    ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
//                    if (this.preFiltered || !allListeners.contains(listener)) {
//                        allListeners.add(listener);
//                    }
                    //这里不知道这个有什么用，如果preFiltered=true，表示不需要预先处理，false表示要处理，
                    //怎么想感觉都很奇怪
                    if (!allListeners.contains(listener)) {
                        allListeners.add(listener);
                    }
                }
            }
            return allListeners;
        }
    }
}
