package org.springframework.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class ClassUtils {

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    //CGLIB动态代理所使用的分隔符
    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * 资料：https://blog.csdn.net/yangcheng33/article/details/52631940
     * 线程上下文类加载器，可以用在需要逆向加载的时候(绕过双亲委派模型的限制)；
     *
     * 该文举例了JDBC的例子来说明这个现象：
     * 1. 通过ServiceLoader这个服务加载器去依赖里面找到符合该接口的jar包
     * ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
     *
     * 2. 但是因为DriverManager是启动类加载器加载的，是不能加载项目依赖的jar包，所以就使用
     * 线程上下文加载器来来加载；加载当前线程的是应用程序类加载器，所以可以加载maven中的依赖
     *      public static <S> ServiceLoader<S> load(Class<S> service) {
     *         ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *         return ServiceLoader.load(service, cl);
     *     }
     * 3. 加载依赖
     * while(driversIterator.hasNext()) {
     *      driversIterator.next();
     * }
     * 这个next最终会进行加载Class.forName(TCCL...)
     *
     * 4. 加载依赖后，会调用实现类中的静态块进行驱动的注册，会放入一个List中保存
     * DriverManager.registerDriver(new Driver());
     * registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
     *
     * 5. 但是在getConnection的时候，具体使用哪个驱动还会进行一个校验，校验的方式就是，使用Class.forName
     * ，传入当前线程的TCCL，如果返回值不为null说明为true
     * isDriverAllowed(aDriver.driver, callerCL)
     * Class.forName(driver.getClass().getName(), true, classLoader);
     * 文中下面有说么为什么要校验：
     *      “tomcat中如果有多个webapp，并且每个都有自己的驱动实现依赖，那么加载的时候就会在不同线程里面，
     *      注册中心也会有很多这样的类，所以为了区分，就需要根据TCCL进行验证”
     *
     *  tomcat四类目录：
     *  1. 类库可以被tomcat和下面的webapp所使用
     *  ==> 组合应用程序类加载器可以被共用 CommonClassLoader
     *  2. 类库只能被tomcat所使用
     *  ==> 组合1中加载器，无子加载器
     *  3. 类库可以被多个webapp所共用，但是tomcat不能使用
     *  4. 只能webapp自己使用
     *  ==> 组合1中加载器，有子加载器，依次往下
     *
     * 热部署：重新new一个新的类加载器，也就是文中说的JasperLoader，当发现jsp变化了，动态更新；
     *
     * 除了tomcat，还有一个spring的例子：
     * 1. 多个web应用程序都使用了spring
     * 2. 那就将其放入共享目录中，而当spring，getBean的时候，就可以访问到具体webapp的实现类
     * 3. 但是实现类的加载范围不在类加载器中
     * ==> 同理也是使用了TCCL
     *
     * copy自文中总结：
     * 1. 当高层提供了统一接口让低层去实现，同时又要是在高层加载（或实例化）低层的类时，
     * 必须通过线程上下文类加载器来帮助高层的ClassLoader找到并加载该类；
     * 2. 当使用本类托管类加载，然而加载本类的ClassLoader未知时，为了隔离不同的调用者，
     * 可以取调用者各自的线程上下文类加载器代为托管。
     */
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

    /**
     * 校验clazz的类加载器或者其父类加载器是否是classLoader
     * 检验的目的就是防止不是该context中的类被缓存；
     * 那如果传入不是该ApplicationContext所管理的类，会怎么样，为什么没有缓存意义？
     */
    public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader){
        Assert.notNull(clazz, "Class must no be null");
        try {
            ClassLoader target = clazz.getClassLoader();
            if(target == null){ //这还能为空的？
                return true;
            }

            ClassLoader cur = classLoader;
            if(cur == target){
                return true;
            }

            while(cur != null){
                cur = cur.getParent();
                if(cur == target){
                    return true;
                }
            }
            return false;
        } catch (SecurityException e) {
            return true; //系统类加载器会报错吗？
        }
    }

    /**
     * 解析出，该类所在包的路径，例如：org.springframework.util.ClassUtils
     * ==> org/springframework/util
     */
    public static String classPackageAsResourcePath(Class<?> clazz){
        if(clazz == null){
            return "";
        }

        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    public static boolean isCglibProxy(Object object){
        return ClassUtils.isCglibProxyClass(object.getClass());
    }

    /**
     * 判断类是否由CGLIB动态生成的代理类，判断该类的类名是否带上$$
     */
    public static boolean isCglibProxyClass(Class<?> clazz){
        return clazz!=null && isCglibProxyClassName(clazz.getName());
    }

    /**
     * 检测指定的类名是否是CGLIB生成的类
     */
    public static boolean isCglibProxyClassName(String className){
        return className != null && className.contains(CGLIB_CLASS_SEPARATOR);
    }

    public static void main(String[] args) throws Exception{
//        System.out.println(ClassUtils.class.getName()); //org.springframework.util.ClassUtils
//
//        //实验jdbc的例子
//        String url = "jdbc:mysql://localhost:3309/test";
//        Connection conn = DriverManager.getConnection(url, "root", "");
//        conn.close();

//        System.out.println(ClassUtils.class.getClassLoader());


    }
}
