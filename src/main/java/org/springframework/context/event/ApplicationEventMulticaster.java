package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * 监听器管理接口，可以将事件发布给多个监听器的；
 * Spring的ApplicationContext就可以委托该类来发布事件；
 */
public interface ApplicationEventMulticaster {

    //添加监听器
    void addApplicationListener(ApplicationListener listener);

    //根据bean name添加
    void addApplicationListenerBean(String listenerBeanName);

    void removeApplicationListener(ApplicationListener listener);

    void removeApplicationListenerBean(String listenerBeanName);

    void removeAllListeners();

    /**
     * 将事件multicast到合适的监听器
     */
    void multicastEvent(ApplicationEvent event);
}