package com;

import org.springframework.context.ApplicationEvent;

public class TestApplicationEvent extends ApplicationEvent {
    public TestApplicationEvent(Object source) {
        super(source);
    }
}
