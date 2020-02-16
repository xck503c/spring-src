package org.springframework.core.io;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * https://www.cnblogs.com/zrtqsk/p/4015323.html
 *
 * 抽象资源文件解析
 * 1. 新方法：File getFile(URI uri)
 * 2. getFileForLastModifiedCheck：会对一些以特定协议开头(jar,zip..)的URL的中间部分进行提取；
 * 这里是假定中间表示jar包的URL是file协议开头的，例如：jar:file:/C:/...A.jar!/com.xck.xxx.class；
 * 最后利用工具类的File getFile(URL resourceUrl, String description)获取Flle；
 */
public abstract class AbstractFileResolvingResource extends AbstractResource{
    public AbstractFileResolvingResource() {}

    @Override
    public File getFile() throws IOException {
        URL url = this.getURL();
        //vfs-虚拟文件系统，这里就先不管这个，假定url开头为file
//        return url.getProtocol().startsWith("vfs") ? AbstractFileResolvingResource.VfsResourceDelegate.getResource(url).getFile() : ResourceUtils.getFile(url, this.getDescription());
        //getDescription用于打印异常信息
        //通过url构建本地File对象
        return ResourceUtils.getFile(url, this.getDescription());
    }

    @Override
    protected File getFileForLastModifiedCheck() throws IOException {
        URL url = this.getURL();
        //判断是否是特定协议的资源文件URL
        if(ResourceUtils.isJarURL(url)){
            //提取中间的，换成file协议
            URL actualUrl = ResourceUtils.extractJarFileURL(url);
//            return actualUrl.getProtocol().startsWith("vfs") ? AbstractFileResolvingResource.VfsResourceDelegate.getResource(actualUrl).getFile() : ResourceUtils.getFile(actualUrl, "Jar URL");
            return ResourceUtils.getFile(actualUrl, "Jar URL");
        }else {
            return this.getFile();
        }
    }

    protected File getFile(URI uri) throws IOException{
//        return uri.getScheme().startsWith("vfs") ? AbstractFileResolvingResource.VfsResourceDelegate.getResource(uri).getFile() : ResourceUtils.getFile(uri, this.getDescription());
        return ResourceUtils.getFile(uri, this.getDescription());
    }


}
