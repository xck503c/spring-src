package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * https://blog.csdn.net/u013412772/article/details/80819314
 * Listable，通过该接口可以一次获取多个bean
 * 1. 根据条件返回bean的集合，而不是像BeanFactory一个个查询
 * 2. 可以返回指定类型的所有beanName
 */
public interface ListableBeanFactory extends BeanFactory{

    //对于给定的bean名是否有bean定义
    boolean containsBeanDefinition(String beanName);

    //返回工厂中BeanDefinition的总数
    int getBeanDefinitionCount();

    // 返回对于指定类型Bean（包括子类）的所有名字
    String[] getBeanNamesForType(Class<?> type);

    //和上面不同的是，加了两个条件
    //includeNonSingletons - 是否只取单例
    //allowEagerInit - 是否需要立即加载
    String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException;

    //查找使用该注解的，返回beanName-beanInstance
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType);

    //根据beanName和注解类型查找注解？
    <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException;
}
