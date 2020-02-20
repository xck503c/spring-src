package org.springframework.core.io;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 默认的资源加载器，如果类加载器没有就用默认的，全都是复用调用
 * 想要拿资源，调用getResource(path)即可
 */
public class DefaultResourceLoader implements ResourceLoader{

    private ClassLoader classLoader;

    //直接用默认的类加载器
    public DefaultResourceLoader() {
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }

    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    //可以设置
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader();
    }

    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        if(location.startsWith("classpath:")){ //若是以classpath:开头就是classpath资源
            return new ClassPathResource(location.substring("classpath:".length()), this.getClassLoader());
        }else { //其余的统一当成URL资源
            try {
                URL url = new URL(location);
                return new UrlResource(url);
            } catch (MalformedURLException var3) {
                //如果不以classpath开头，但是又不是合法URL，那就使用classpathContext类，但是本质还是ClassPathResource
                //通过location拿到资源对象
                return this.getResourceByPath(location);
            }
        }
    }

    protected Resource getResourceByPath(String path) {
        return new DefaultResourceLoader.ClassPathContextResource(path, this.getClassLoader());
    }

    private static class ClassPathContextResource extends ClassPathResource implements ContextResource{
        public ClassPathContextResource(String path, ClassLoader classLoader) {
            super(path, classLoader);
        }

        @Override
        public String getPathWithinContext() {
            return this.getPath();
        }

        @Override
        public Resource createRelative(String relativePath) {
            //工具类直接复用
            String pathToUse = StringUtils.applyRelativePath(this.getPath(), relativePath);
            return new DefaultResourceLoader.ClassPathContextResource(pathToUse, this.getClassLoader());
        }
    }
}
