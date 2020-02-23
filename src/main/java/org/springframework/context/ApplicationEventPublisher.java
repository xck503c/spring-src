package org.springframework.context;

/**
 * 封装事件发布者的功能，是Application的父接口之一
 */
public interface ApplicationEventPublisher {
    //通知监听器事件发布了
    //在github上，这里被换成了Object，因为有可能实现的类并不是ApplicationEvent

    /**
     * 通知所有注册的监听器，该事件发生了，事件可以是spring定义事件，也可以是自定义
     */
    void publishEvent(ApplicationEvent event);
}
