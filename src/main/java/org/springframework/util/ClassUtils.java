package org.springframework.util;

public class ClassUtils {

    public static ClassLoader getDefaultClassLoader(){
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            //不能访问？为啥不能访问
            // Cannot access thread context ClassLoader - falling back...
        }

        if(cl == null){
            //没有当前线程的上下文类加载器，使用当前类的类加载器
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader(); //应用程序类加载器
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    public static void main(String[] args) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getContextClassLoader());
            }
        });
        System.out.println(Thread.currentThread().getContextClassLoader());
        t.start();
        System.out.println(ClassLoader.getSystemClassLoader());
        //sun.misc.Launcher$AppClassLoader@18b4aac2
        //sun.misc.Launcher$AppClassLoader@18b4aac2
        //sun.misc.Launcher$AppClassLoader@18b4aac2
    }
}
