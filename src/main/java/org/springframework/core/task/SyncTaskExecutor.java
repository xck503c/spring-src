package org.springframework.core.task;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 该类的主要目的是为了测试
 */
public class SyncTaskExecutor implements TaskExecutor, Serializable {

    /**
     * 可以看到这个直接同步调用
     * @param task
     */
    @Override
    public void execute(Runnable task) {
        Assert.notNull(task, "Runnable must not be null");
        task.run();
    }
}
