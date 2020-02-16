package org.springframework.beans.factory;

/**
 * Listable，通过该接口可以一次获取多个bean
 * 1. 提供bean的迭代功能，而不是通过
 */
public interface ListableBeanFactory extends BeanFactory{

    //对于给定的bean名是否有bean定义
    boolean containsBeanDefinition(String beanName);

    //返回工厂中BeanDefinition的总数
    int getBeanDefinitionCount();

    // 返回对于指定类型Bean（包括子类）的所有名字
    String[] getBeanNamesForType(Class<?> type);

    String[] getBeanNamesForType();
}
