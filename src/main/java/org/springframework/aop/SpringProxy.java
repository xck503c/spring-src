package org.springframework.aop;

/**
 * 一个标记接口，很有趣的用法，所有AOP代理都实现了这个标记接口；
 * 它的作用就是用来区分是否是spring生成的代理
 */
public interface SpringProxy {

}
