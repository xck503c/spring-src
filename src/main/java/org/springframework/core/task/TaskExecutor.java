package org.springframework.core.task;

import java.util.concurrent.Executor;

/**
 * 因为Executor是1.5出来的，所以为了可以和标准对接，又能向后兼容所以才这样，我猜的
 */
public interface TaskExecutor extends Executor {
    @Override
    void execute(Runnable task);
}
