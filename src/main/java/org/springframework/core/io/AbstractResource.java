package org.springframework.core.io;

import org.springframework.core.NestedIOException;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 基础的抽象资源类
 * https://www.cnblogs.com/zrtqsk/p/4015323.html
 * 1. 唯一的getInputStream()留给子类实现
 * 2. contentLength方法，打开文件流，读取一遍来判断字节数
 * 3. getDescription() 描述符留给子类实现
 *
 */
public abstract class AbstractResource implements Resource{
    public AbstractResource(){}

    @Override
    public boolean exists(){
        try {
            return getFile().exists();
        } catch (IOException e) {
            try {
                //getInputStream实现在子类中
                //判断产生异常，关闭输入流，可以正常关闭说明存在
                InputStream is = getInputStream();
                is.close();
                return true;
            } catch (Throwable isEx) {
                return false;
            }
        }
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    //抽象中不支持，给子类覆盖
    @Override
    public URL getURL() throws IOException{
        throw new FileNotFoundException(this.getDescription() + " cannot be resolved to URL");
    }

    @Override
    public URI getURI() throws IOException{
        URL url = this.getURL();
        //将url格式化
        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException e) {
            throw new NestedIOException("Invalid URI [" + url + "]", e);
        }
    }

    //这个资源内容长度实际就是资源的字节长度，通过全部读取一遍来判断。这个方法调用起来很占资源啊！
    @Override
    public long contentLength() throws IOException{
        InputStream is = this.getInputStream();
        Assert.state(is!=null, "resource input stream must not be null");

        try {
            long size = 0;
            byte[] buf = new byte[255]; //为什么是255？
            int read;
            while((read = is.read(buf)) != -1){
                size += read;
            }
            return size;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public long lastModified() throws IOException{
        long lastModified = this.getFileForLastModifiedCheck().lastModified();
        if (lastModified == 0L) {
            throw new FileNotFoundException(this.getDescription() + " cannot be resolved in the file system for resolving its last-modified timestamp");
        } else {
            return lastModified;
        }
    }

    protected File getFileForLastModifiedCheck() throws IOException{
        return this.getFile();
    }

    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(this.getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + this.getDescription());
    }

    @Override
    public String getFilename() {
        return null;
    }

    //toString就是文件描述符
    public String toString() {
        return this.getDescription();
    }

    public boolean equals(Object obj) {
        return obj == this || obj instanceof Resource && ((Resource)obj).getDescription().equals(this.getDescription());
    }

    public int hashCode() {
        return this.getDescription().hashCode();
    }
}
