package org.springframework.core.io;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * https://www.cnblogs.com/zrtqsk/p/4015323.html
 *
 * 抽象资源文件解析
 * 1. 新方法：File getFile(URI uri)
 * 2. getFileForLastModifiedCheck：会对一些以特定协议开头(jar,zip..)的URL的中间部分进行提取；
 * 这里是假定中间表示jar包的URL是file协议开头的，例如：jar:file:/C:/...A.jar!/com.xck.xxx.class；
 * 最后利用工具类的File getFile(URL resourceUrl, String description)获取File；
 * 3. 并未实现getURL方法
 * 4. boolean exists()为什么还包含了网络资源？我一直以为这个方法只会有本地文件，难道是为了可以复用？
 * 下面这个这篇文家里的解释，感觉可以说明这个疑问
 *
 * https://blog.csdn.net/fyzlucky2015/article/details/77943994,这里面有个解释不错：
 * 1. 该类代表需要解析的路径资源；
 * 2. 带有路径解析的资源类似这样，比如：
 * http://....; ftp://.......; file://......; classpath://....; jar://........;war://.......；
 * 3. 该类使用Java的统一资源定位符，URL对象，来定位资源，来标识如何访问这类资源。
 *
 * https://github.com/spring-projects/spring-framework/blob/master/spring-core：
 * Abstract base class for resources which resolve URLs into File references,
 * such as UrlResource or ClassPathResource.
 * Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 * 该类是一个抽象资源的基础类，用于解析URL为文件引用；它可以在URL中检测file协议和jboss的vfs协议
 * 来提取出文件系统的引用；
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

    @Override
    public boolean exists(){
        try {
            URL url = this.getURL();
            if(ResourceUtils.isFileURL(url)){
                return this.getFile().exists();
            }else{
                //此处还未真正进行网络连接
                //如果是http会返回HttpURLConnection，jar-JarURLConnection
                URLConnection con = url.openConnection();
                this.customizeConnection(con);
                HttpURLConnection httpCon = con instanceof HttpURLConnection ? (HttpURLConnection)con : null;
                if(httpCon != null){
                    int code = httpCon.getResponseCode(); //发现内部有调用getInputStream
                    if (code == 200) {
                        return true;
                    }

                    if (code == 404) {
                        return false;
                    }
                }

                if(con.getContentLength()>=0){ //有content-length header
                    return true;
                }else if(httpCon != null){ //其余错误码...
                    httpCon.disconnect();
                    return false;
                } else {
                    InputStream is = this.getInputStream(); //尝试打开inputstream，如果可以就判断存在？
                    is.close();
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        try {
            URL url = this.getURL();
            if (!ResourceUtils.isFileURL(url)) {
                return true;
            } else {
                File file = this.getFile();
                return file.canRead() && !file.isDirectory();
            }
        } catch (IOException var3) {
            return false;
        }
    }

    @Override
    public long contentLength() throws IOException {
        URL url = this.getURL();
        if (ResourceUtils.isFileURL(url)) {
            return this.getFile().length();
        } else {
            URLConnection con = url.openConnection();
            this.customizeConnection(con);
            return (long)con.getContentLength();
        }
    }

    @Override
    public long lastModified() throws IOException {
        URL url = this.getURL();
        if(!ResourceUtils.isFileURL(url) && !ResourceUtils.isJarURL(url)){
            URLConnection con = url.openConnection();
            this.customizeConnection(con);
            return con.getLastModified(); //header-last-modified
        }else {
            return super.lastModified();
        }
    }

    protected void customizeConnection(URLConnection con) throws IOException{
        ResourceUtils.useCachesIfNecessary(con);
        if(con instanceof HttpURLConnection){
            customizeConnection((HttpURLConnection)con);
        }
    }

    /**
     * 自定义HttpURLConnection，在exists，contentLength，lastModified方法调用，
     * 默认设置请求方法HEAD，可以在子类中重写
     * HEAD：类似于get请求，只不过返回的响应中没有具体的内容，用于获取报头
     */
    protected void customizeConnection(HttpURLConnection con) throws IOException{
        con.setRequestMethod("HEAD");
    }

    public static void main(String[] args) throws Exception{
        URL url = new URL("http://www.baidu.com");
        URLConnection con = url.openConnection();
        System.out.println(con.getContentLength());  //2381 里面有getInputStream
        //每次getInputStream好像都会connect一次
        InputStream is = con.getInputStream();
    }
}
