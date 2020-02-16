package org.springframework.core;

import java.io.IOException;

//嵌套IOException
public class NestedIOException extends IOException {
    public NestedIOException(String msg){
        super(msg);
    }

    public NestedIOException(String msg, Throwable cause){
        super(msg);
        this.initCause(cause);
    }

    public String getMessage(){
        return NestedExceptionUtils.buildMessage(super.getMessage(), this.getCause());
    }

    static {
        NestedExceptionUtils.class.getName(); //这是什么操作，难道是提前加载这个class？
    }
}
