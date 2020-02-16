package org.springframework.core.io;

/**
 * https://blog.csdn.net/u010086122/article/details/81607167
 * 1. 为了更方便获取资源
 * 2. 开发人员可以不需要知道Resource实现类，也无需自己创建
 */
public interface ResourceLoader {
    String CLASSPATH_URL_PREFIX = "classpath:";

    Resource getResource(String location);

    ClassLoader getClassLoader();
}
