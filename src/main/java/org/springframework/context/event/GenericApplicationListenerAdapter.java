package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

/**
 * SmartApplicationListener的适配器，用于确定是否支持事件的
 */
public class GenericApplicationListenerAdapter implements SmartApplicationListener{

    //委托人?
    private final ApplicationListener delegate;

    public GenericApplicationListenerAdapter(ApplicationListener delegate) {
        Assert.notNull(delegate, "Delegate listener must not be null");
        this.delegate = delegate;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        this.delegate.onApplicationEvent(event);
    }

    /**
     * 在这里又使用了泛型解析器，封装好了各种方法;
     * 1. 获取监听器泛型中的类型是什么
     * 2. 经过代理对象，使用AopUtils.getTargetClass获取代理类的Class
     * 3. 判断该类型是否是所需判断事件的父类或者父接口，是就表示支持，子类可以转成父类，但是父类不一定可以转成子类；
     * @param eventType
     * @return
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
        //代理类？为什么这样可以判断可能是代理类
        if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
            //...这里先不管，后面再说
        }

        //找不到就是默认支持？
        return (typeArg == null || typeArg.isAssignableFrom(eventType));
    }

    //无论什么都支持
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }
}
