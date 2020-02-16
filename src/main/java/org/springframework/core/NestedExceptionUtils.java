package org.springframework.core;

public abstract class NestedExceptionUtils {
    public NestedExceptionUtils() {}

    //拼接嵌套异常信息，格式：message; nested exception is Exception...
    public static String buildMessage(String message, Throwable cause){
        if(cause!=null){
            StringBuilder sb = new StringBuilder();
            if(message!=null){
                sb.append(message).append("; ");
            }
            sb.append("nested exception is ").append(cause);
            return sb.toString();
        }else{
            return message;
        }
    }
}
