package org.springframework.core.io.support;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

/**
 * https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/support/ResourcePatternResolver.java
 *
 * 解析资源的策略接口
 */
public interface ResourcePatternResolver extends ResourceLoader {

    //会在所有classpath下寻找
    String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    //根据路径模式，返回资源集合
    Resource[] getResources(String locationPattern) throws IOException;
}
