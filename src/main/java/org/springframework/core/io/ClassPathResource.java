package org.springframework.core.io;

import com.sun.istack.internal.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
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


}
