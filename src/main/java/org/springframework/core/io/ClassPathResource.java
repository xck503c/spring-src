package org.springframework.core.io;

import com.sun.istack.internal.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 使用给定的ClassLoader或Class来加载资源
 */
public class ClassPathResource extends AbstractFileResolvingResource{

    private final String path; //资源路径

    @Nullable
    private ClassLoader classLoader;

    @Nullable
    private Class<?> clazz;

    public ClassPathResource(String path){
        this(path, (ClassLoader)null);
    }

    public ClassPathResource(String path, @Nullable ClassLoader classLoader){
        Assert.notNull(path, "path must not be null");
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    public ClassPathResource(String path, Class<?> clazz) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    protected ClassPathResource(String path, ClassLoader classLoader, Class<?> clazz) {
        this.path = StringUtils.cleanPath(path);
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    public final String getPath() {
        return this.path;
    }

    public final ClassLoader getClassLoader() {
        return this.clazz != null ? this.clazz.getClassLoader() : this.classLoader;
    }

    //解析基础类路径资源的URL
    protected URL resolveURL(){
        if(this.clazz != null){
            return this.clazz.getResource(this.path);
        } else {
            return classLoader!=null?this.classLoader.getResource(path):ClassLoader.getSystemResource(path);
        }
    }

    @Override
    public boolean exists() {
        return (resolveURL() != null);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        } else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    @Override
    public URL getURL() throws IOException {
        URL url = resolveURL();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    public Resource createRelative(String relativePath){
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new ClassPathResource(pathToUse, classLoader, clazz);
    }

    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }

    public String getDescription(){
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = this.path;
        if (this.clazz != null && !pathToUse.startsWith("/")) { //相对路径？
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) { //引用相同那就不用比较了
            return true;
        }
        if (!(other instanceof ClassPathResource)) {
            return false;
        }
        ClassPathResource otherRes = (ClassPathResource) other;
        return (this.path.equals(otherRes.path) &&
                ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader) &&
                ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
    }


    public int hashCode() {
        return this.path.hashCode();
    }

    public static void main(String[] args) {
        /**
         * 1. 若传入相对路径：根据当前类的相对路径去找，若找不到，返回null
         * 2. 若使用绝对路径
         * (1) 会先去启动类加载器加载的范围中找资源
         * (2) 再去根据项目classpath的跟路径去找
         */
        //file:/D:/xck/spring-src/target/classes/org/springframework/core/io/
        System.out.println(ClassPathResource.class.getResource(""));
        //file:/D:/xck/spring-src/target/classes/org/springframework/core/
        System.out.println(ClassPathResource.class.getResource(".."));
        //file:/D:/xck/spring-src/target/classes/
        System.out.println(ClassPathResource.class.getResource("/"));
        //jar:file:/C:/Program%20Files/Java/jdk1.8.0_191/jre/lib/deploy.jar!/com
        System.out.println(ClassPathResource.class.getResource("/com"));
        //若在目录下建立一个com.Test.class，可以找得到
        System.out.println(ClassPathResource.class.getResource("/com/Test.class"));

        /**
         * 系统类加载器，不能带/:
         * 1. 会现在启动类加载的范围下找
         * 2. 再去项目的classpath下找
         */
        //file:/D:/xck/spring-src/target/classes/
        System.out.println(ClassLoader.getSystemClassLoader().getResource(""));
        //jar:file:/C:/Program%20Files/Java/jdk1.8.0_191/jre/lib/deploy.jar!/com
        System.out.println(ClassLoader.getSystemClassLoader().getResource("com"));
        //jar:file:/C:/Program%20Files/Java/jdk1.8.0_191/jre/lib/rt.jar!/com/oracle/net/Sdp.class
        System.out.println(ClassPathResource.class.getClassLoader().getResource("com/oracle/net/Sdp.class"));
        //jar:file:/C:/Program%20Files/Java/jdk1.8.0_191/jre/lib/rt.jar!/java/lang/Byte.class
        System.out.println(ClassLoader.getSystemClassLoader().getResource("java/lang/Byte.class"));
    }
}
