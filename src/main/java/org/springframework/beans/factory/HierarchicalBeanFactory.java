package org.springframework.beans.factory;

public interface HierarchicalBeanFactory extends BeanFactory{
    //返回该工厂的父工厂
    BeanFactory getParentBeanFactory();

    //该工厂是否包含该bean
    boolean containsLocalBean(String name);
}
