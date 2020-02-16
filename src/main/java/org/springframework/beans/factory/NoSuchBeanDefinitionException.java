package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

public class NoSuchBeanDefinitionException extends BeansException {

    public NoSuchBeanDefinitionException(String msg){
        super(msg);
    }
}
