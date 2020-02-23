package com;

import org.springframework.context.ApplicationListener;

import java.io.Serializable;

public class TestApplicationListener extends Test<Object>
        implements ApplicationListener<TestApplicationEvent>, Serializable {

    @Override
    public void onApplicationEvent(TestApplicationEvent event) {

    }
}
