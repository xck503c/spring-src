package org.springframework.context;

public interface ApplicationEventPublisher {
    //通知监听器事件发布了
    //在github上，这里被换成了Object，因为有可能实现的类并不是ApplicationEvent
    void publishEvent(ApplicationEvent event);
}
