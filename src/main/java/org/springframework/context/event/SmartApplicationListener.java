package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * 标准接口的扩展，目的是可以自定义判断事件类型；
 * 注释部分原文：exposing further metadata such as the supported event type
 */
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent> {

    /**
     * 确定(Determine)该监听器是否支持该事件
     */
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

    boolean supportsSourceType(Class<?> sourceType);
}
