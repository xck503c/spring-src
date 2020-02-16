package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * https://blog.csdn.net/u013412772/article/details/80755463
 * 基本IOC容器接口:
 * 1. 工厂模式的工厂接口，spring本质是一个bean工厂(容器)；
 * 2. 它按照我们的要求生产各种bean，在生产的过程中为了解决依赖问题，
 *  才使用了DI技术；
 * 3. 好处：
 * (1)松耦合：通过引入第三方来装配
 * (2)更好管理：spring会在bean生命周期的各个阶段对bean进行各种管理，
 * 然后将这些接口暴露给我们，让我们可以对bean进行处理；我们是需要实现
 * 对应的接口，spring就会根据我们的实现来处理bean
 */
public interface BeanFactory {
    /**
     * 转义符？
     */
    String FACTORY_BEAN_PREFIX = "&";

    //根据beanName获取bean
    Object getBean(String name) throws BeansException;

    //根据beanName和class获取bean
    <T> T getBean(String name, Class<T> clzz) throws BeansException;

    <T> T getBean(Class<T> clazz) throws BeansException;

    //是否存在
    boolean containsBean(String name);

    //是否是单例
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

    //是否是prototype
    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

    //是否匹配类型
    boolean isTypeMatch(String name, Class<?> clazz) throws NoSuchBeanDefinitionException;

    //获取bean的Class
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    //获取bean的别名
    String[] getAliases(String name);
}
