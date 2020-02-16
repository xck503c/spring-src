package org.springframework.util;

//断言类
public class Assert {
    public Assert(){}

    public static void notNull(Object object, String message){
        if(object == null){
            throw new IllegalArgumentException(message);
        }
    }

    //状态断言
    public static void state(boolean expression, String message){
        if(!expression){ //断言不成功
            throw new IllegalStateException(message);
        }
    }
}
