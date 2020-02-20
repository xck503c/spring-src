package org.springframework.context;

import java.util.EventListener;

/**
 * 事件监听器，观察者模式
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    /**
     * 处理事件
     */
    void onApplicationEvent(E event);
}
