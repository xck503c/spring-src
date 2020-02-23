package org.springframework.context;

import java.util.EventObject;

/**
 * 所有application event的始祖，应用事件的抽象类
 * 既然jdk提供了，就直接继承
 */
public abstract class ApplicationEvent extends EventObject {
    private static final long serialVersionUID = 7099057708183571937L;
    //事件发生的时间
    private final long timestamp;
    //事件源
    public ApplicationEvent(Object source) {
        super(source);
        timestamp = System.currentTimeMillis();
    }

    public final long getTimestamp() {
        return this.timestamp;
    }
}
