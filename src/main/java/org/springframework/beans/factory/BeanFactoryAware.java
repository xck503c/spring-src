package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * 实现该接口的bean，希望感知到生产它的BeanFactory
 * 1. 依赖查找：
 * 2. 依赖注入：
 */
public interface BeanFactoryAware extends Aware{
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
