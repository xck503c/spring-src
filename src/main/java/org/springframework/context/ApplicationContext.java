package org.springframework.context;

import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * ListableBeanFactory, HierarchicalBeanFactory都继承了
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
    //返回application context的唯一ID
    @Nullable
    String getId();

    //返回context一个通俗易懂的名字
    String getDisplayName();

    //context启动一次加载的时间戳
    long getStartupDate();

    @Nullable
    ApplicationContext getParent();

    //AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
}
