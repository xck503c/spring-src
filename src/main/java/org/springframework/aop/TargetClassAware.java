package org.springframework.aop;

/**
 * 所有Aop代理对象或者代理工厂的实现接口，该接口有两个扩展接口：
 * 1. org.springframework.aop.framework.Advised
 * 2. TargetSources
 * 该接口用于暴露出被代理目标对象类型；
 */
public interface TargetClassAware {
    Class<?> getTargetClass();
}
